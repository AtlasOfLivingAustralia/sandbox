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

class DataCheckController {

  def biocacheService
  def darwinCoreService
  def authService
  def tikaService
  def fileService
  def formatService
  def collectoryService

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
    def fileId = params.fileId
    def firstLineIsData = params.boolean('firstLineIsData')

    //the data to pass back
    List<ParsedRecord> processedRecords = new ArrayList<ParsedRecord>()

    def counter = 0
    def csvReader = csvData ? fileService.getCSVReaderForText(csvData) : fileService.getCSVReaderForFile(fileService.getFileForFileId(fileId))
    def currentLine = csvReader.readNext()
    if(firstLineIsData){
      counter += 1
      processedRecords.add(biocacheService.processRecord(headers, currentLine))
    }

    currentLine = csvReader.readNext()

    while(currentLine != null && counter <noOfRowsToDisplay){
      counter += 1
      processedRecords.add(biocacheService.processRecord(headers, currentLine))
      currentLine = csvReader.readNext()
    }

    // add formatted data for ui
    processedRecords.each { pr ->
      pr.values.each { v ->
        v.camelCaseName = formatService.prettyCamel(v.name)
        v.formattedProcessed = formatService.formatProperty(v.processed)
      }
      pr.assertions.each { a ->
        a.name = message(code: a.name, default: a.name)
      }
    }

    def instance = [processedRecords:processedRecords]
    respond(instance, view:'processedData',  model:[processedRecords:processedRecords])
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
    if (csvData) {
      String separator = fileService.getSeparatorName(csvData)
      responseString = biocacheService.uploadData(csvData, headers, datasetName, separator, firstLineIsData, customIndexedFields, dataResourceUid)
    } else {
      String separator = fileService.getSeparatorName(fileService.getFileForFileId(fileId))
      responseString = biocacheService.uploadFile(fileId, headers, datasetName, separator, firstLineIsData, customIndexedFields, dataResourceUid)
    }

    def biocacheResponse = JSON.parse(responseString)

    // on a successful response, save a copy of the file under the UID
    def inputStream = csvData ? IOUtils.toInputStream(csvData, 'UTF-8') : fileService.getFileForFileId(fileId).newInputStream()
    fileService.saveArchiveCopy(biocacheResponse.uid, inputStream)

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
      response.sendError(404, uid ? "Archive file for $uid not found" : "${fileID}.csv.zip not found")
    }

    response.setHeader("Cache-Control", "must-revalidate")
    response.setHeader("Pragma", "must-revalidate")
    response.setHeader("Content-Disposition", "attachment;filename=\"${file.name}\"")
    response.contentType = "application/zip"
    response.contentLength = file.length()

    def output = response.outputStream
    output.withStream {
      file.withInputStream { input ->
        output << input
      }
    }
    null
  }
}