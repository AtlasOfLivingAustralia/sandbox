package au.org.ala.datacheck

import au.com.bytecode.opencsv.CSVReader
import au.org.ala.datacheck.UploadService.UploadException
import grails.converters.JSON
import org.springframework.web.multipart.MultipartHttpServletRequest

import java.nio.file.Paths

import static javax.servlet.http.HttpServletResponse.*

class DataCheckController {

    def biocacheService
    def darwinCoreService
    def authService
    def fileService
    def formatService
    def collectoryService
    def uploadService
    def tagService
    def collectoryHubRestService

    // data columns more than the header
    static String COLSIZE_MISMATCH = "colSizeMismatch"
    static String DUPLICATE_HEADER = "colHeaderDuplicate"
    static String DUPLICATE_KEYFIELDS = "duplicateKeyFieldValues"
    static String MISSING_KEYFIELDS = "newuploadMissingKeyFields"
    static String MISSING_KEYFIELDS_REUPLOAD = "reuploadMissingKeyFields"

    static allowedMethods = [processData: "POST"]

    def noOfRowsToDisplay = 5

    def index() {
        def redirectToSandbox = params.containsKey('redirectToSandbox')
        def model = [dataResource: [:], reload: false, file: [id: params.fileId, name: params.fileName], redirectToSandbox: redirectToSandbox]
        respond model, view: '/sandbox/index', model: model
    }

    def reload() {
        def dataResource = collectoryService.getTempResourceMetadata(params.dataResourceUid)
        def model = [reload: true, dataResource: dataResource, file: [id: '', name: ''], redirectToSandbox: false]
        respond(dataResource, view: "/sandbox/index", model: model)
    }

    def parseColumns() {

        log.debug("Content type>>" + request.getContentType())
        request.getHeaderNames().each { x -> log.debug(x + ": " + request.getHeader(x)) }

        //is it comma separated or tab separated
        def raw = params.rawData
        raw = raw.replaceAll("[\\t]\"[\\t]") { a -> a.replace("\"", "\"\"") }

        def fileId = params.fileId
        def firstLineIsData = params.boolean('firstLineIsData')

        log.debug("Unparsed RAW>> $raw")

        if (!raw && !fileId) {
            response.sendError(400, "must provide raw paste data or a file id")
            return
        }

        CSVReader csvReader = raw ? fileService.getCSVReaderForText(raw) : fileService.getCSVReaderForFile(fileService.getFileForFileId(fileId))

        //determine column headers
        def columnHeadersUnparsed = csvReader.readNext()

        log.debug("Unparsed>> " + columnHeadersUnparsed)

        def columnHeaders = null
        def columnHeaderMap = null
        def dataRows = new ArrayList<String[]>()

        //guess at the first line if the request didn't specify the first line is data
        if (firstLineIsData == null) {
            if (biocacheService.areColumnHeaders(columnHeadersUnparsed)) {
                log.debug("First line of data recognised as darwin core terms")
                firstLineIsData = false
                columnHeaderMap = biocacheService.mapColumnHeaders(columnHeadersUnparsed)
            } else {
                //first line is data
                log.debug("First line of data is assumed to be data")
                firstLineIsData = true
                dataRows.add(columnHeadersUnparsed)
                columnHeaders = biocacheService.guessColumnHeaders(columnHeadersUnparsed)
            }
        } else {
            if (firstLineIsData) {
                log.debug("First line of data is assumed to be data")
                dataRows.add(columnHeadersUnparsed)
                columnHeaders = biocacheService.guessColumnHeaders(columnHeadersUnparsed)
            } else {
                //first line is data
                log.debug("First line of data recognised as darwin core terms")
                columnHeaderMap = biocacheService.mapColumnHeaders(columnHeadersUnparsed)
            }
        }

        // homogenise the columns
        def headers
        if (columnHeaders) {
            def unknownCount = 0
            headers = columnHeaders.collect {
                if (it) [header: it, known: true]
                else [header: "Unknown ${unknownCount++}", known: false]
            }
        } else {
            headers = columnHeaderMap.collect { entry ->
                [header: entry.value ?: entry.key, known: !!entry.value, original: entry.key]
            }
        }

        log.debug("Parsed>> " + columnHeaders + ", size: " + columnHeaders)

        def startAt = firstLineIsData ? 0 : 1

        def currentLine = csvReader.readNext()
        for (int i = startAt; i < startAt + noOfRowsToDisplay && currentLine != null; i++) {
            dataRows.add(currentLine)
            currentLine = csvReader.readNext()
        }
        // pass back HTML table
        if (firstLineIsData) {
            final instance = [headers: headers, columnHeaders: columnHeaders, dataRows: dataRows, firstLineIsData: firstLineIsData]
            respond(instance, view: 'parsedData', model: instance)
        } else {
            final instance = [headers: headers, columnHeaderMap: columnHeaderMap, dataRows: dataRows, firstLineIsData: firstLineIsData]
            respond(instance, view: 'parsedData', model: instance)
        }
    }

    def uploadFile() {

        def f = ((MultipartHttpServletRequest) request).getFile('myFile')
        if (f.empty) {
            response.sendError(400, 'file cannot be empty')
            return
        }

        def dataResourceUid = params.dataResourceUid
        if (dataResourceUid) {
            log.info "Loading data resource ${dataResourceUid}"
        }

        try {
            def instance = uploadService.uploadFile(dataResourceUid, f)
            respond instance
        } catch (UploadException e) {
            log.error("Upload exception for uid: $dataResourceUid", e)
            response.sendError(400, e.message)
        }
    }

    def processData() {

        def headers = null
        if (params.headers) {
            headers = params.headers
            if (headers instanceof String) {
                headers = headers.split(',')
            }
        }

        def csvData = params.rawData?.trim()
        csvData = csvData.replaceAll("[\\t]\"[\\t]") { a -> a.replace("\"", "\"\"") }

        def fileId = params.fileId
        def firstLineIsData = params.boolean('firstLineIsData')

        String dataResourceUid = params.dataResourceUid

        String keyField = ""
        Boolean reload
        if (dataResourceUid) {
            Map tempMetaData = collectoryService.getTempResourceMetadata(dataResourceUid)
            keyField = tempMetaData?.keyFields ?: ""
            reload = true
        } else {
            keyField = uploadService.getKeyFieldFromHeader(headers)
            reload = false
        }

        //the data to pass back
        List<ParsedRecord> processedRecords = new ArrayList<ParsedRecord>()

        def counter = 0
        def csvReader = csvData ? fileService.getCSVReaderForText(csvData) : fileService.getCSVReaderForFile(fileService.getFileForFileId(fileId))
        def currentLine = csvReader.readNext()

        def rawHeader = null

        List<ParsedRecord> readList = new ArrayList<ParsedRecord>()

        if (firstLineIsData) {
            counter += 1
            def messages = performPreviewValidation(readList, rawHeader, headers, currentLine, true, keyField, reload)
            ParsedRecord pr = biocacheService.processRecord(headers, currentLine)
            processedRecords.add(pr)
            pr.validationMessages = messages?.toArray() ?: new ValidationMessage[0]
        } else {
            rawHeader = currentLine
        }

        currentLine = csvReader.readNext()

        while (currentLine != null && counter < noOfRowsToDisplay) {
            counter += 1
            def messages = performPreviewValidation(readList, rawHeader, headers, currentLine, true, keyField, reload)
            ParsedRecord pr = biocacheService.processRecord(headers, currentLine)
            processedRecords.add(pr)
            pr.validationMessages = messages?.toArray() ?: new ValidationMessage[0]
            currentLine = csvReader.readNext()
        }

        List<String> validationMsg = new ArrayList<String>()

        // add formatted data for ui
        processedRecords.each { pr ->
            pr.values.each { v ->
                v.camelCaseName = formatService.prettyCamel(v.name)
                v.formattedProcessed = formatService.formatProperty(v.processed)
            }
            pr.validationMessages.each { a ->
                a.message = message(code: a.code, args: a.args, default: a.code)
                if (!validationMsg.contains(a.message)) {
                    validationMsg.add(a.message)
                }
            }
            pr.assertions.each { a ->
                a.name = message(code: a.name, default: a.name)
            }
        }

        // Check the rest of the rows which are not displayed
        while (currentLine != null && validationMsg.size() == 0) {
            def messages = performPreviewValidation(readList, rawHeader, headers, currentLine, false, keyField, reload)
            messages.each { m ->
                validationMsg.add(message(code: m.code, args: m.args, default: m.code))
            }
            currentLine = csvReader.readNext()
        }

        def instance = [processedRecords: processedRecords, validationMessages: validationMsg]
        respond(instance, view: 'processedData', model: instance)

    }

    private ParsedRecord storeValues(def headers, def currentLine) {
        ParsedRecord parsedRecord = new ParsedRecord()

        List<ProcessedValue> processedValues = new ArrayList<ProcessedValue>()

        for (int i = 0; i < headers.size(); i++) {
            ProcessedValue processedValue = new ProcessedValue()
            processedValue.name = headers[i]
            if (i < currentLine.size()) {
                processedValue.raw = currentLine[i]
            } else {
                processedValue.raw = ""
            }
            processedValues.add(processedValue)
        }

        parsedRecord.values = processedValues.toArray()

        parsedRecord
    }

    private performHeaderValidation(List<ValidationMessage> messages,
                                    def rawHeader, def headers, String keyField, Boolean reload) {
        List<String> rawHeaderList = Arrays.asList(rawHeader)
        List<String> headerList = Arrays.asList(headers)

        if (reload) {
            if (keyField) {
                // Check if required keyHeader is present
                def keyHeader = headerList.find { it.toLowerCase().trim() == keyField.toLowerCase() }
                if (!keyHeader) {
                    ValidationMessage vm = new ValidationMessage(MISSING_KEYFIELDS_REUPLOAD, keyField)
                    messages.add(vm)
                }
            }
        } else {
            if (!keyField) {
                ValidationMessage vm = new ValidationMessage(MISSING_KEYFIELDS, keyField)
                messages.add(vm)
            }
        }

        def trimHeaderList = headerList.collect { it.toLowerCase().trim().replaceAll("\\s+", " ") }
        def dupHeader = trimHeaderList.countBy { it }.findAll { it.value > 1 }*.key

        if (dupHeader.size() > 0) {
            List<String> strArgs = new ArrayList<String>()
            for (String s : headerList) {
                if (dupHeader.contains(s.toLowerCase().trim().replaceAll("\\s+", " "))) {
                    strArgs.add(s)
                }
            }
            ValidationMessage vm = new ValidationMessage(DUPLICATE_HEADER, strArgs.toString())
            messages.add(vm)
        } else if (headerList.size() < rawHeaderList.size()) {
            //  sometimes, duplicate headers in raw header can cause processed headers to be removed...therefore we check on raw headers for duplicates
            def trimRawHeaderList = rawHeaderList.collect { it.toLowerCase().trim().replaceAll("\\s+", " ") }
            def dupRawHeader = trimRawHeaderList.countBy { it }.findAll { it.value > 1 }*.key

            if (dupRawHeader.size() > 0) {
                List<String> strArgs = new ArrayList<String>()
                // This is to get back the actual raw text
                for (String s : rawHeaderList) {
                    if (dupRawHeader.contains(s.toLowerCase().trim().replaceAll("\\s+", " "))) {
                        strArgs.add(s)
                    }
                }
                ValidationMessage vm = new ValidationMessage(DUPLICATE_HEADER, strArgs.toString())
                messages.add(vm)
            }
        }
    }

    private validateKeyFieldValues(
            def processedRecords, def pr, List<ValidationMessage> messages, String keyFieldColumn, boolean reupload) {

        String missingMessageCode = reupload ? MISSING_KEYFIELDS : MISSING_KEYFIELDS_REUPLOAD

        if (!messages.find { it.code == missingMessageCode }) {

            def rawKeyArray = pr.values.grep { it.name == keyFieldColumn }
            def rawValue = ""
            if (rawKeyArray.size()) {
                rawValue = rawKeyArray.raw?.get(0) ?: ""
            }

            if ("".equals(rawValue.trim())) {
                List<String> strArgs = new ArrayList<String>()
                strArgs.add(keyFieldColumn)
                ValidationMessage vm = new ValidationMessage(missingMessageCode, strArgs.toString())
                messages.add(vm)
            }

            /*
             def rawValue = pr.values.grep { it.name == column }?.raw?.size() > 0 ? pr.values.grep {
               it.name == column
             }?.raw.get(0) : ""*/

            def dupList = processedRecords.findAll {
                it.values.findAll { a -> ((a.name == keyFieldColumn) && (a.raw == rawValue)) }
            }

            if (dupList.size() > 0) {
                List<String> strArgs = new ArrayList<String>()
                strArgs.add(keyFieldColumn + " " + rawValue)
                ValidationMessage vm = new ValidationMessage(DUPLICATE_KEYFIELDS, strArgs.toString())
                messages.add(vm)
            }

        }
    }

    private performHeaderRecordValidation(List<ValidationMessage> messages,
                                          def rawHeader, def headers, def currentLine) {
        List<String> headerList = Arrays.asList(headers)
        // If all headers can be filled, check headers that it is not blank
        if (headerList.size() == currentLine.size()) {
            def testHeaderList = headerList.findAll { it.trim() == "" }
            if (testHeaderList.size() > 0) {
                ValidationMessage vm = new ValidationMessage(COLSIZE_MISMATCH, "")
                messages.add(vm)
            }
        } else {
            List<String> rawHeaderList = Arrays.asList(rawHeader)
            def testHeaderList = rawHeaderList.findAll { it.trim() == "" }
            if ((testHeaderList.size() > 0) || (currentLine.size() != rawHeader.size())) {
                testHeaderList.removeAll { (it.trim() == "") }
                if (testHeaderList.size() > 0) {
                    ValidationMessage vm = new ValidationMessage(COLSIZE_MISMATCH, " " + testHeaderList.toString())
                    messages.add(vm)
                } else {
                    ValidationMessage vm = new ValidationMessage(COLSIZE_MISMATCH, "")
                    messages.add(vm)
                }
            }
        }
    }

    private checkDuplicateRecords(
            def processedRecords,
            def pr, List<ValidationMessage> messages, String column, def currentLine, def messageCode) {
        def rawValue = pr.values.grep { it.name == column }?.raw?.size() > 0 ? pr.values.grep {
            it.name == column
        }?.raw.get(0) : ""

        def dupList = processedRecords.findAll {
            it.values.findAll { a -> ((a.name == column) && (a.raw == rawValue)) }
        }

        if (dupList.size() > 0) {
            List<String> strArgs = new ArrayList<String>()
            strArgs.add(rawValue)
            ValidationMessage vm = new ValidationMessage(messageCode, strArgs.toString())
            messages.add(vm)

        }
    }

    private performPreviewValidation(List<ParsedRecord> readList,
                                     def rawHeader,
                                     def headers, def currentLine, def forPreview, String keyField, Boolean reload) {

        List<ValidationMessage> messages = new ArrayList<ValidationMessage>()

        // Check for Header and data columns mismatch and whether it contains duplicates. Only if there's a raw header is provided
        if (rawHeader && grailsApplication.config.validation.mandatory) {
            performHeaderRecordValidation(messages, rawHeader, headers, currentLine)
            if (forPreview) {
                performHeaderValidation(messages, rawHeader, headers, keyField, reload)
            }

            ParsedRecord readRecord = storeValues(headers, currentLine)

            validateKeyFieldValues(readList, readRecord, messages, keyField, reload)

            //   checkDuplicateRecords(readList, readRecord, messages, OCCURRENCE_ID_COLUMN, DUPLICATE_OCCURRENCEID)

            //   checkDuplicateRecords(readList, readRecord, messages, CATALOG_NUMBER_COLUMN, DUPLICATE_CATALOGNUMBER)

            readList.add(readRecord)

            return messages

        }
    }

    def upload() {

        def userId = authService.getUserId()
        if (!userId) {
            response.sendError(401)
            response.setHeader("X-Sandbox-Authenticated", "false")
            response.setHeader("X-Sandbox-Authorised", "false")
            return null
        }

        if (grailsApplication.config.clubRole) {
            //check roles
            def isInClub = authService.userInRole(grailsApplication.config.clubRole)
            if (!isInClub) {
                response.setHeader("X-Sandbox-Authenticated", "true")
                response.setHeader("X-Sandbox-Authorised", "false")
                response.sendError(401)
                return null
            }
        }

        //read the csv
        String headers
        def headersVal = params.headers
        if (headersVal instanceof String) {
            headers = headersVal
        } else {
            headers = headersVal?.join(',')?.trim()
        }
        String csvData = request.getParameter("rawData")?.trim()
        String fileId = request.getParameter("fileId")?.trim()
        String datasetName = request.getParameter("datasetName")?.trim()
        String customIndexedFields = request.getParameter("customIndexedFields")?.trim()
        String firstLineIsData = request.getParameter("firstLineIsData")
        String dataResourceUid = request.getParameter('dataResourceUid')

        def responseString
        String separator, separatorChar
        if (csvData) {
            separatorChar = fileService.getSeparator(csvData)
            separator = fileService.getSeparatorName(separatorChar)
            responseString = biocacheService.uploadData(csvData, headers, datasetName, separator, firstLineIsData, customIndexedFields, dataResourceUid)
        } else {
            separatorChar = fileService.getSeparator(fileService.getFileForFileId(fileId))
            separator = fileService.getSeparatorName(separatorChar)
            responseString = biocacheService.uploadFile(fileId, headers, datasetName, separator, firstLineIsData, customIndexedFields, dataResourceUid)
        }

        def biocacheResponse = JSON.parse(responseString)

        // on a successful response, save a copy of the file under the UID
        def reader = csvData ? new StringReader(csvData) : fileService.getFileForFileId(fileId).newReader('UTF-8')
        fileService.saveArchiveCopy(biocacheResponse.uid, reader, headers, firstLineIsData == 'true', separator)

        //update temp data resource with separator and key field value
        Map drt = [csvSeparator: separatorChar]
        String drtId = biocacheResponse.uid ?: dataResourceUid
        if (!dataResourceUid) {
            drt.keyFields = uploadService.getKeyFieldFromHeader(headers?.split(','))
        }
        Map saveTempResp = collectoryHubRestService.saveTempDataResource(drt, drtId)
        log.debug(saveTempResp.toMapString())

        response.setContentType("application/json")
        render(responseString)
    }

    def redirectToBiocache() {
        redirect(url: uploadService.biocacheUrl(params.uid))
    }

    def redirectToSpatialPortal() {
        redirect(url: uploadService.spatialPortalUrl(params.uid))
    }

    def redirectToDownload() {
        if (grailsApplication.config.download.offline) {
            redirect(url: uploadService.downloadUrl(params.uid))
        } else {
            redirect(url: uploadService.downloadOfflineUrl(params.uid))
        }
    }


    def uploadStatus() {
        log.debug("Request to retrieve upload status")
        response.setContentType("application/json")

        def responseString = ''
        if (params.uid) {
            responseString = biocacheService.uploadStatus(params.uid)

            //store status by tag
            if (params.tag && responseString) {
                //add uid
                def json = JSON.parse(responseString)
                json.uid = params.uid

                tagService.put(params.tag, json.toString())
            }
        } else if (params.tag) {
            //retrieve status
            responseString = tagService.get(params.tag)
        }

        render(responseString)
    }

    def autocomplete() {
        def query = params.q
        def list = darwinCoreService.autoComplete(query, 10)
        respond(list)
    }

    def serveFile() {
        def fileId = params.fileId
        def uid = params.uid
        File file
        if (uid) {
            file = fileService.getArchiveCopy(uid)
        } else if (fileId) {
            file = Paths.get(grailsApplication.config.uploadFilePath, fileId, fileId + '.csv.zip').toFile()
        } else {
            response.sendError(SC_BAD_REQUEST, "No uid or fileId provided")
            return
        }

        if (!file.exists()) {
            response.sendError(SC_NOT_FOUND, uid ? "Archive file for $uid not found" : "${fileId}.csv.zip not found")
            return
        }

        response.setHeader("Cache-Control", "must-revalidate")
        response.setHeader("Pragma", "must-revalidate")

        final fileModified = file.lastModified()
        if (request.getDateHeader('If-Modified-Since') >= fileModified) {
            render status: SC_NOT_MODIFIED
            return
        }

        response.setHeader("Content-Disposition", "attachment;filename=\"${file.name}\"")
        response.contentType = "application/zip"
        response.contentLength = (int) file.length()
        response.setDateHeader('Last-Modified', fileModified)

        if (request.method == "HEAD") {
            render status: SC_OK
            return
        }
        def output = response.outputStream
        file.withInputStream { input ->
            output << input
        }
        null
    }
}