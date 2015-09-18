package au.org.ala.datacheck

import au.com.bytecode.opencsv.CSVReader
import grails.converters.JSON
import groovy.json.JsonSlurper
import org.apache.commons.io.FileUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class UploadController {

    def tikaService
    def fileService
    def biocacheService
    def collectoryService
    def authService
    def tagService

    def rowsToPreview = 5

    def reload(){
        def dataResource = collectoryService.getTempResourceMetadata(params.dataResourceUid)
        render(view:"index", model:[reload:true, dataResource: dataResource ])
    }

    def uploadToSandbox() {

        def userId = authService.getUserId()
        if(!userId){
            response.sendError(401)
            return null
        }

        def js = new JsonSlurper()
        def jsonPayload = request.reader.text
        def json = js.parseText(jsonPayload)
        def responseString = biocacheService.uploadFile(
                json.fileId,
                json.headers.trim(),
                json.datasetName?:"".trim(),
                "COMMA",
                json.firstLineIsData.trim(),
                "",
                json.dataResourceUid)
        response.setContentType("application/json")
        if (json.tag) {
            tagService.put(json.tag, (JSON.parse(responseString) as Map) + [fileId: json.fileId])
        }
        render(responseString)
    }

    def uploadFile() {

        def f = request.getFile('myFile')
        if (f.empty) {
            flash.message = 'file cannot be empty'
            render(view: 'uploadForm')
            return
        }

        def dataResourceUid = params.dataResourceUid
        if(dataResourceUid) {
            log.info "Loading data resource ${dataResourceUid}"
        }

        def fileId = System.currentTimeMillis()
        def uploadDirPath = grailsApplication.config.uploadFilePath + fileId
        log.debug "Creating upload directory " + uploadDirPath
        def uploadDir = new File(uploadDirPath)
        FileUtils.forceMkdir(uploadDir)

        log.debug "Transferring file to directory...."
        def newFile = new File(uploadDirPath + File.separatorChar + f.getFileItem().getName())
        f.transferTo(newFile)

        log.debug "Detecting file formats...."
        def contentType = fileService.detectFormat(newFile)

        log.debug "Content type...." + contentType
        //if its GZIPPED or ZIPPED extract the file
        if (contentType == "application/zip") {
            //upzip it
            def result = fileService.extractZip(newFile)
            if (result.success) {
                newFile = result.file
                contentType = fileService.detectFormat(result.file)
            } else {
                flash.message = result.message
                render(view: 'uploadForm')
            }
        } else if (contentType == "application/x-gzip") {
            def result = fileService.extractGZip(newFile)
            if (result.success) {
                newFile = result.file
                contentType = fileService.detectFormat(result.file)
            } else {
                flash.message = result.message
                render(view: 'uploadForm')
            }
        }

        def extractedFile = new File(uploadDirPath + File.separatorChar + fileId + '.csv')

        if (contentType.startsWith("text")) {
            //extract and re-write into common CSV format
            log.debug("Is a CSV....")
            FileUtils.copyFile(newFile, extractedFile)
        } else {
            //extract the data
            def csvWriter = new au.com.bytecode.opencsv.CSVWriter(new FileWriter(extractedFile))
            String extractedString = tikaService.parseFile(newFile)
            //HTML version of the file
            Document doc = Jsoup.parse(extractedString);
            Elements dataTable = doc.select("tr");

            if (dataTable?.size() > 0) {
                def cellCount = dataTable?.get(0)?.select("td")?.size() ?: 0
                dataTable.each { tr ->
                    def cells = tr.select("td")
                    def fields = new String[cellCount]
                    if (cells) {
                        cells.eachWithIndex { cell, idx ->
                            if (idx < cellCount) {
                                fields[idx] = cell.text().trim()
                            }
                        }
                    }
                    csvWriter.writeNext(fields)
                }
                csvWriter.close()
            }
        }

        //create a zipped version for uploading.....
        fileService.zipFile(extractedFile)

        //redirect to upload
        def p = [fn:newFile.getName()]
        if (dataResourceUid) {
            p.putAll([dataResourceUid: dataResourceUid, datasetName: params.datasetName])
        }
        if (params.tag) p.putAll([tag: params.tag])
        redirect([controller: 'upload', action: 'preview', id: fileId, params: p])
    }

    def preview() {
        [id: params.id, userId: authService.getUserId()]
    }

    def parseColumnsWithFirstLineInfo = {

        //is it comma separated or tab separated
        def raw = readTopOfFile(params.id)
        log.debug("FirstLineIsData: " + params.firstLineIsData)

        def firstLineIsData = Boolean.parseBoolean(request.getParameter("firstLineIsData").trim())

        //determine column headers
        def columnHeadersUnparsed = raw[0]

        log.debug("Unparsed >> " + columnHeadersUnparsed)

        def columnHeaders = null
        def columnHeaderMap = null
        def dataRows = []

        //is the first line a set of column headers ??
        if (firstLineIsData) {
            log.debug("First line of data is assumed to be data")
            dataRows << columnHeadersUnparsed
            columnHeaders = biocacheService.guessColumnHeaders(columnHeadersUnparsed)
        } else {
            //first line is data
            log.debug("First line of data recognised as darwin core terms")
            columnHeaderMap = biocacheService.mapColumnHeaders(columnHeadersUnparsed)
        }

        log.debug("Parsed >> " + columnHeaders)
        def startAt = firstLineIsData ? 0 : 1

        for (int i = startAt; i < raw.size(); i++) {
            dataRows << raw[i]
        }
        // pass back HTML table
        if (firstLineIsData) {
            render(view: '../dataCheck/parsedData', model: [columnHeaders: columnHeaders, dataRows: dataRows, firstLineIsData: firstLineIsData])
        } else {
            render(view: '../dataCheck/parsedData', model: [columnHeaderMap: columnHeaderMap, dataRows: dataRows, firstLineIsData: firstLineIsData])
        }
    }

    def serveFile = {

        response.setHeader("Cache-Control", "must-revalidate");
        response.setHeader("Pragma", "must-revalidate");
        response.setHeader("Content-Disposition", "attachment;filename=${params.fileId}.csv.zip");
        response.setContentType("application/zip");

        def output = response.outputStream
        def input = new FileInputStream(grailsApplication.config.uploadFilePath +  params.fileId + File.separatorChar + params.fileId + '.csv.zip')
        def bytes = new byte[10240]
        def len = -1
        while ((len = input.read(bytes, 0, bytes.length)) != -1){
          output.write(bytes, 0, len)
        }
        output.close()
        input.close()
        null
    }

    def readTopOfFile(fileId) {

        def extractedFile = new File(grailsApplication.config.uploadFilePath + File.separatorChar + fileId + File.separatorChar + fileId + '.csv')

        log.debug("Content type >> " + request.getContentType())
        request.getHeaderNames().each {
            x -> log.debug(x + ": " + request.getHeader(x))
        }

        //is it comma separated or tab separated
        def raw = []
        CSVReader rdr = new CSVReader(new FileReader(extractedFile), (char)',', (char)'"')
        (0..rowsToPreview).each {
            def nextLine = rdr.readNext()
            if(nextLine){
                raw << nextLine
            }
        }
        raw
    }

    def parseColumns = {

        log.debug('Parsing columns for ID: ' + params.id)

        log.debug("Content type >> " + request.getContentType())

        //is it comma separated or tab separated
        def raw = readTopOfFile(params.id)

        //def raw = request.getParameter("rawData").trim()
        log.debug("Unparsed RAW >> " + raw)

        //determine column headers
        def columnHeadersUnparsed = raw[0]

        log.debug("Unparsed >> " + columnHeadersUnparsed)

        def columnHeaders = null
        def columnHeaderMap = null
        def firstLineIsData = !biocacheService.areColumnHeaders(columnHeadersUnparsed)
        def dataRows = []

        //is the first line a set of column headers ??
        if (firstLineIsData) {
            //first line is data
            log.debug("First line of data is assumed to be data")
            columnHeaders = biocacheService.guessColumnHeaders(columnHeadersUnparsed)
        } else {
            log.debug("First line of data recognised as darwin core terms")
            columnHeaderMap = biocacheService.mapColumnHeaders(columnHeadersUnparsed)
        }

        log.debug("Parsed >> " + columnHeaders + ", size: " + columnHeaders)

        def startAt = firstLineIsData ? 0 : 1

        for (int i = startAt; i <= raw.size(); i++) {
            dataRows << raw[i]
        }

        // pass back HTML table
        if (firstLineIsData) {
            render(view: '../dataCheck/parsedData', model: [columnHeaders: columnHeaders, dataRows: dataRows, firstLineIsData: firstLineIsData])
        } else {
            render(view: '../dataCheck/parsedData', model: [columnHeaderMap: columnHeaderMap, dataRows: dataRows, firstLineIsData: firstLineIsData])
        }
    }

    def viewProcessData() {

        def extractedFile = new File('/data/datacheck/uploads/' + params.id + File.separatorChar + params.id + '.csv')

        def headers = null
        if (params.headers) {
            headers = params.headers.split(",")
        }

        def csvData = ""

        def buffReader = new BufferedReader(new FileReader(extractedFile))
        (0..rowsToPreview).each {
            csvData += (buffReader.readLine() + '\n')
        }

        def firstLineIsData = Boolean.parseBoolean(params.firstLineIsData ?: 'false')

        //the data to pass back
        List<ParsedRecord> processedRecords = new ArrayList<ParsedRecord>()

        def counter = 0
        def csvReader = fileService.getCSVReaderForText(csvData)
        def currentLine = csvReader.readNext()
        if (firstLineIsData) {
            counter += 1
            processedRecords.add(biocacheService.processRecord(headers, currentLine))
        }

        currentLine = csvReader.readNext()

        while (currentLine != null && counter < rowsToPreview) {
            counter += 1
            processedRecords.add(biocacheService.processRecord(headers, currentLine))
            currentLine = csvReader.readNext()
        }

        render(view: '../dataCheck/processedData', model: [processedRecords: processedRecords])
    }

    def index() {}
}