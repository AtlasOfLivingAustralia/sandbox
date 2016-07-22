package au.org.ala.datacheck

import au.com.bytecode.opencsv.CSVReader
import grails.converters.JSON
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

import java.nio.file.Paths

import static javax.servlet.http.HttpServletResponse.*

class DataCheckController {

  def biocacheService
  def darwinCoreService
  def authService
  def tikaService
  def fileService
  def formatService
  def collectoryService

  // data columns more than the header
  static String COLSIZE_MISMATCH = "colSizeMismatch"
  static String DUPLICATE_HEADER = "colHeaderDuplicate"
  static String DUPLICATE_CATALOGNUMBER = "catalogNumberDuplicate"
  static String DUPLICATE_OCCURRENCEID = "duplicateOccurrenceId"

  static allowedMethods = [processData: "POST"]

  def noOfRowsToDisplay = 5

  def index() {
    def model = [existing: [:], reload: false]
    respond model, view: 'index', model: model
  }

  def reload() {
    def dataResource = collectoryService.getTempResourceMetadata(params.dataResourceUid)

    respond(dataResource, view:"index", model:[reload:true, dataResource: dataResource ])
  }

  def parseColumns() {

    log.debug("Content type>>" + request.getContentType())
    request.getHeaderNames().each { x -> log.debug(x + ": " + request.getHeader(x))}

    //is it comma separated or tab separated
    def raw = params.rawData
    raw = raw.replaceAll("[\\t]\"[\\t]") {a -> a.replace("\"", "\"\"")}

    def fileId = params.fileId
    def firstLineIsData = params.boolean('firstLineIsData')

    //def raw = request.getParameter("rawData").trim()
    log.debug("Unparsed RAW>> $raw")

    if (!raw && !fileId) {
      response.sendError(400, "must provide raw paste data or a file id")
      return
    }

    CSVReader csvReader = raw ? fileService.getCSVReaderForText(raw) : fileService.getCSVReaderForFile(fileService.getFileForFileId(fileId))

    //determine column headers
    def columnHeadersUnparsed = csvReader.readNext()

    log.debug("Unparsed>> "  + columnHeadersUnparsed)

    def columnHeaders = null
    def columnHeaderMap = null
    def dataRows = new ArrayList<String[]>()

    //guess at the first line if the request didn't specify the first line is data
    if (firstLineIsData == null) {
      if(biocacheService.areColumnHeaders(columnHeadersUnparsed)) {
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
      if(firstLineIsData) {
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

    log.debug("Parsed>> "  + columnHeaders + ", size: " + columnHeaders)

    def startAt = firstLineIsData ? 0 : 1

    def currentLine = csvReader.readNext()
    for(int i = startAt; i < startAt + noOfRowsToDisplay && currentLine != null; i++) {
      dataRows.add(currentLine)
      currentLine = csvReader.readNext()
    }
    // pass back HTML table
    if (firstLineIsData) {
      final instance = [headers: headers, columnHeaders:columnHeaders, dataRows:dataRows, firstLineIsData:firstLineIsData]
      respond(instance, view:'parsedData',  model: instance)
    } else {
      final instance = [headers: headers, columnHeaderMap:columnHeaderMap, dataRows:dataRows, firstLineIsData:firstLineIsData]
      respond(instance, view:'parsedData',  model: instance)
    }
  }

  def uploadFile() {

    def f = request.getFile('myFile')
    if (f.empty) {
      response.sendError(400, 'file cannot be empty')
      return
    }

    def dataResourceUid = params.dataResourceUid
    if (dataResourceUid) {
      log.info "Loading data resource ${dataResourceUid}"
    }

    def fileId = UUID.randomUUID().toString()
    def uploadDir = new File(grailsApplication.config.uploadFilePath, fileId)
    log.debug "Creating upload directory $uploadDir"
    FileUtils.forceMkdir(uploadDir)

    log.debug "Transferring file to directory..."
    def newFile = new File(uploadDir, f.getFileItem().getName())
    f.transferTo(newFile)

    log.debug "Detecting file formats..."
    def contentType = fileService.detectFormat(newFile)

    log.debug "Content type.... $contentType"
    //if its GZIPPED or ZIPPED extract the file
    if (contentType == "application/zip") {
      //upzip it
      def result = fileService.extractZip(newFile)
      if (result.success) {
        newFile = result.file
        contentType = fileService.detectFormat(result.file)
      } else {
        response.sendError(400, result.message)
        return
      }
    } else if (contentType == "application/x-gzip") {
      def result = fileService.extractGZip(newFile)
      if (result.success) {
        newFile = result.file
        contentType = fileService.detectFormat(result.file)
      } else {
        response.sendError(400, result.message)
        return
      }
    }

    def extractedFile = new File(uploadDir, fileId + '.csv')

    if (contentType.startsWith("text")) {
      //extract and re-write into common CSV format
      log.debug("Is a CSV....")
      FileUtils.copyFile(newFile, extractedFile)
    } else {
      //extract the data
      def csvWriter = new au.com.bytecode.opencsv.CSVWriter(new FileWriter(extractedFile))
      String extractedString = tikaService.parseFile(newFile)
      //HTML version of the file
      Document doc = Jsoup.parse(extractedString)
      Elements dataTable = doc.select("tr")

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

    //create a zipped version for uploading
    fileService.zipFile(extractedFile)

    def instance = [fileId: fileId, fileName: newFile.name]
    respond instance
  }

  def processData() {

    def headers = null
    if(params.headers){
      headers = params.headers
      if (headers instanceof String) {
        headers = headers.split(',')
      }
    }

    def csvData = params.rawData?.trim()
    csvData = csvData.replaceAll("[\\t]\"[\\t]") {a -> a.replace("\"", "\"\"")}

    def fileId = params.fileId
    def firstLineIsData = params.boolean('firstLineIsData')

    //the data to pass back
    List<ParsedRecord> processedRecords = new ArrayList<ParsedRecord>()

    def counter = 0
    def csvReader = csvData ? fileService.getCSVReaderForText(csvData) : fileService.getCSVReaderForFile(fileService.getFileForFileId(fileId))
    def currentLine = csvReader.readNext()

    def rawHeader = null

    if(firstLineIsData){
      counter += 1
      ParsedRecord pr = biocacheService.processRecord(headers, currentLine)
      performRecordValidation (processedRecords, pr, rawHeader, headers, currentLine)
      processedRecords.add(pr)
    } else {
      rawHeader = currentLine
    }

    currentLine = csvReader.readNext()

    while(currentLine != null && counter <noOfRowsToDisplay){
      counter += 1
      ParsedRecord pr = biocacheService.processRecord(headers, currentLine)
      performRecordValidation (processedRecords, pr, rawHeader, headers, currentLine)
      processedRecords.add(pr)
      currentLine = csvReader.readNext()
    }

    // add formatted data for ui
    processedRecords.each { pr ->
      pr.values.each { v ->
        v.camelCaseName = formatService.prettyCamel(v.name)
        v.formattedProcessed = formatService.formatProperty(v.processed)
      }
      pr.validationMessages.each { a ->
        a.message = message(code: a.code, args: a.args, default: a.code)
      }
      pr.assertions.each { a ->
        a.name = message(code: a.name, default: a.name)
      }
    }

    def instance = [processedRecords:processedRecords]
    respond(instance, view:'processedData',  model: instance)
  }

  private performRecordValidation (List<ParsedRecord> processedRecords, ParsedRecord pr, def rawHeader, def headers, def currentLine) {

    List<ValidationMessage> messages = new ArrayList<ValidationMessage>()

    // Check for Header and data columns mismatch and whether it contains duplicates. Only if there's a raw header is provided
    if (rawHeader) {
      List<String> rawHeaderList = Arrays.asList(rawHeader)
      def testHeaderList = rawHeaderList.findAll{(it.trim()=="")}
      if ((testHeaderList.size() > 0) || (currentLine.size() != rawHeader.size())) {
        testHeaderList.removeAll {(it.trim()=="")}
        if (testHeaderList.size() > 0) {
          ValidationMessage vm = new ValidationMessage(COLSIZE_MISMATCH, testHeaderList.toString())
          messages.add(vm)
        } else {
          //ValidationMessage vm = new ValidationMessage(COLSIZE_MISMATCH, new ArrayList<String>().toString())
          ValidationMessage vm = new ValidationMessage(COLSIZE_MISMATCH, "")
          messages.add(vm)
        }
      }

      List<String> headerList = Arrays.asList(headers)
      def trimHeaderList = headerList.collect {it.toLowerCase().trim().replaceAll("\\s+", " ")}
      def dupHeader = trimHeaderList.countBy {it}.findAll{it.value > 1}*.key

      if (dupHeader.size() > 0) {
        List<String> strArgs = new ArrayList<String>()
        // This is to get back the actual raw text
        for (String s: headerList) {
          if (dupHeader.contains(s.toLowerCase().trim().replaceAll("\\s+", " "))) {
            strArgs.add(s)
          }
        }
        ValidationMessage vm = new ValidationMessage(DUPLICATE_HEADER, strArgs.toString())
        messages.add(vm)
      } else if (headerList.size() < rawHeaderList.size())  {
        //  sometimes, duplicate headers in raw header can cause processed headers to be removed...therefore we check on raw headers for duplicates
        def trimRawHeaderList = rawHeaderList.collect {it.toLowerCase().trim().replaceAll("\\s+", " ")}
        def dupRawHeader = trimRawHeaderList.countBy {it}.findAll{it.value > 1}*.key

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

    def rawCatalogNumber = pr.values.grep{it.name=="catalogNumber"}?.raw?.size() > 0 ? pr.values.grep{it.name=="catalogNumber"}?.raw.get(0) : ""

    def dupCatalogNumberList = processedRecords.findAll {it.values.findAll{a-> ((a.name=="catalogNumber") && (a.raw == rawCatalogNumber))}}

    if (dupCatalogNumberList.size() > 0) {
      List<String> strArgs = new ArrayList<String>()
      strArgs.add(rawCatalogNumber)
      ValidationMessage vm = new ValidationMessage(DUPLICATE_CATALOGNUMBER, strArgs.toString())
      messages.add(vm)
    }

    def occurrenceId = pr.values.grep{it.name=="occurrenceId"}?.raw?.size() > 0 ? pr.values.grep{it.name=="occurrenceId"}?.raw.get(0) : ""

    def dupOccurrenceIdList = processedRecords.findAll {it.values.findAll{a-> ((a.name=="occurrenceId") && (a.raw == occurrenceId))}}

    if (dupOccurrenceIdList.size() > 0) {
      List<String> strArgs = new ArrayList<String>()
      strArgs.add(occurrenceId)
      ValidationMessage vm = new ValidationMessage(DUPLICATE_OCCURRENCEID, strArgs.toString())
      messages.add(vm)
    }

    pr.validationMessages = messages.toArray()

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
    String separator
    if (csvData) {
      separator = fileService.getSeparatorName(csvData)
      responseString = biocacheService.uploadData(csvData, headers, datasetName, separator, firstLineIsData, customIndexedFields, dataResourceUid)
    } else {
      separator = fileService.getSeparatorName(fileService.getFileForFileId(fileId))
      responseString = biocacheService.uploadFile(fileId, headers, datasetName, separator, firstLineIsData, customIndexedFields, dataResourceUid)
    }

    def biocacheResponse = JSON.parse(responseString)

    // on a successful response, save a copy of the file under the UID
    def reader = csvData ? new StringReader(csvData) : fileService.getFileForFileId(fileId).newReader('UTF-8')
    fileService.saveArchiveCopy(biocacheResponse.uid, reader, headers, firstLineIsData == 'true', separator)

    response.setContentType("application/json")
    render(responseString)
  }

  def redirectToBiocache() {
    def http = new HttpClient()
    //reference the UID caches
    def get = new GetMethod(grailsApplication.config.sandboxHubsWebapp + "/occurrences/refreshUidCache")
    http.executeMethod(get)
    redirect(url:grailsApplication.config.sandboxHubsWebapp + "/occurrences/search?q=data_resource_uid:" + params.uid)
  }

  def redirectToSpatialPortal() {
    def http = new HttpClient()
    //reference the UID caches
    def get = new GetMethod(grailsApplication.config.sandboxHubsWebapp + "/occurrences/refreshUidCache")
    http.executeMethod(get)
    redirect(url:grailsApplication.config.spatialPortalUrl + "?q=data_resource_uid:" + params.uid + grailsApplication.config.spatialPortalUrlOptions)
  }

  def redirectToDownload() {
    def http = new HttpClient()
    //reference the UID caches
    def get = new GetMethod(grailsApplication.config.sandboxHubsWebapp + "/occurrences/refreshUidCache")
    http.executeMethod(get)
    //redirect(url:grailsApplication.config.biocacheServiceUrl + "/occurrences/index/download?q=data_resource_uid:" + params.uid + grailsApplication.config.biocacheServiceDownloadParams)
    redirect(url:grailsApplication.config.biocacheServiceUrl + "/occurrences/index/download?reasonTypeId=" + grailsApplication.config.downloadReasonId + "&q=data_resource_uid:" + params.uid + "&" + grailsApplication.config.biocacheServiceDownloadParams)
  }


  def uploadStatus() {
    log.debug("Request to retrieve upload status")
    def responseString = biocacheService.uploadStatus(params.uid)
    response.setContentType("application/json")
    render(responseString)
  }

  def autocomplete() {
    def query = params.q
    //def limit = params.limit !=null ? params.limit.asType(Integer.class) : 10
    def list = darwinCoreService.autoComplete(query, 10)
    respond(list)
  }

  def serveFile() {
    def fileId = params.fileId
    def uid = params.uid
    File file
    if (uid) {
      file = fileService.getArchiveCopy(uid)
    } else {
      file = Paths.get(grailsApplication.config.uploadFilePath, fileId, fileId + '.csv.zip').toFile()
    }

    if (!file.exists()) {
      response.sendError(SC_NOT_FOUND, uid ? "Archive file for $uid not found" : "${fileID}.csv.zip not found")
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