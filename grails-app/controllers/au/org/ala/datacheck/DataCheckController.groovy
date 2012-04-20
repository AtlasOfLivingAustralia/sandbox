package au.org.ala.datacheck

import au.com.bytecode.opencsv.CSVReader
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.PostMethod

import org.apache.commons.httpclient.NameValuePair
import org.apache.commons.httpclient.methods.GetMethod

class DataCheckController {

  def biocacheService
  def darwinCoreService

  static allowedMethods = [processData: "POST"]

  def noOfRowsToDisplay = 5

  def index = {
    redirect(uri:"../index")
  }

  def parseColumns = {

    println("Content type>>" + request.getContentType())
    request.getHeaderNames().each { x -> println(x + ": " + request.getHeader(x))}

    //is it comma separated or tab separated
    def raw = request.getReader().readLines().join("").trim()

    //def raw = request.getParameter("rawData").trim()
    println("Unparsed RAW>> "  + raw)

    CSVReader csvReader = getCSVReaderForText(raw)

    //determine column headers
    def columnHeadersUnparsed = csvReader.readNext()

    println("Unparsed>> "  + columnHeadersUnparsed)

    def columnHeaders = null
    def columnHeaderMap = null
    def firstLineIsData  = false
    def dataRows = new ArrayList<String[]>()

    //is the first line a set of column headers ??
    if(biocacheService.areColumnHeaders(columnHeadersUnparsed)){
      println("First line of data recognised as darwin core terms")
      firstLineIsData = false
      columnHeaderMap = biocacheService.mapColumnHeaders(columnHeadersUnparsed)
    } else {
      //first line is data
      println("First line of data is assumed to be data")
      firstLineIsData = true
      dataRows.add(columnHeadersUnparsed)
      columnHeaders = biocacheService.guessColumnHeaders(columnHeadersUnparsed)
    }

    println("Parsed>> "  + columnHeaders + ", size: " + columnHeaders)

    def startAt = firstLineIsData ? 0 : 1

    def currentLine = csvReader.readNext()
    for(int i=startAt; i<noOfRowsToDisplay && currentLine!=null; i++){
      dataRows.add(currentLine)
      currentLine = csvReader.readNext()
    }
    // pass back HTML table
    if(firstLineIsData){
      render(view:'parsedData',  model:[columnHeaders:columnHeaders, dataRows:dataRows, firstLineIsData:firstLineIsData])
    } else {
      render(view:'parsedData',  model:[columnHeaderMap:columnHeaderMap, dataRows:dataRows, firstLineIsData:firstLineIsData])
    }
  }

  def parseColumnsWithFirstLineInfo = {

    //is it comma separated or tab separated
    def raw = request.getParameter("rawData").trim()
    def firstLineIsData = Boolean.parseBoolean(request.getParameter("firstLineIsData").trim())
    CSVReader csvReader = getCSVReaderForText(raw)

    //determine column headers
    def columnHeadersUnparsed = csvReader.readNext()

    println("Unparsed>> "  + columnHeadersUnparsed)

    def columnHeaders = null
    def columnHeaderMap = null
    def dataRows = new ArrayList<String[]>()

    //is the first line a set of column headers ??
    if(firstLineIsData){
      println("First line of data is assumed to be data")
      dataRows.add(columnHeadersUnparsed)
      columnHeaders = biocacheService.guessColumnHeaders(columnHeadersUnparsed)
    } else {
      //first line is data
      println("First line of data recognised as darwin core terms")
      columnHeaderMap = biocacheService.mapColumnHeaders(columnHeadersUnparsed)
    }

    println("Parsed>> "  + columnHeaders)
    def startAt = firstLineIsData ? 0 : 1

    def currentLine = csvReader.readNext()
    for(int i=startAt; i<noOfRowsToDisplay && currentLine!=null; i++){
      dataRows.add(currentLine)
      currentLine = csvReader.readNext()
    }
    // pass back HTML table
    if(firstLineIsData){
      render(view:'parsedData',  model:[columnHeaders:columnHeaders, dataRows:dataRows, firstLineIsData:firstLineIsData])
    } else {
      render(view:'parsedData',  model:[columnHeaderMap:columnHeaderMap, dataRows:dataRows, firstLineIsData:firstLineIsData])
    }
  }

  def getCSVReaderForText(String raw) {
    def separator = getSeparator(raw)
    def csvReader = new CSVReader(new StringReader(raw), separator.charAt(0))
    csvReader
  }

  def getSeparator(String raw) {
    int tabs = raw.count("\t")
    int commas = raw.count(",")
    if(tabs > commas)
      return '\t'
    else
      return ','
  }

  def getSeparatorName(String raw) {
    int tabs = raw.count("\t")
    int commas = raw.count(",")
    if(tabs > commas)
      return "TAB"
    else
      return "COMMA"
  }

  def processData = {

    def headers = null
    if(params.headers){
        headers = params.headers.split(",")
    }

    def csvData = params.rawData.trim()
    def firstLineIsData = Boolean.parseBoolean(params.firstLineIsData)

    //the data to pass back
    List<ParsedRecord> processedRecords = new ArrayList<ParsedRecord>()

    def counter = 0
    def csvReader = getCSVReaderForText(csvData)
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

    render(view:'processedData',  model:[processedRecords:processedRecords])
  }

  def upload = {
    //read the csv
    String headers = request.getParameter("headers").trim()
    String csvData = request.getParameter("rawData").trim()
    String separator = getSeparatorName(csvData)
    String datasetName = request.getParameter("datasetName").trim()
    String firstLineIsData = request.getParameter("firstLineIsData")
    def responseString = biocacheService.uploadData(csvData,headers,datasetName,separator,firstLineIsData)
    response.setContentType("application/json")
    render(responseString)
  }

  def redirectToBiocache = {
    def http = new HttpClient()
    //reference the UID caches
    def get = new GetMethod("http://sandbox.ala.org.au/hubs-webapp/occurrences/refreshUidCache")
    http.executeMethod(get)
    redirect(url:"http://sandbox.ala.org.au/hubs-webapp/occurrences/search?q=data_resource_uid:"+params.uid)
  }

  def uploadStatus = {
    log.debug("Request to retrieve upload status")
    def responseString = biocacheService.uploadStatus(params.uid)
    response.setContentType("application/json")
    render(responseString)
  }

  def autocomplete = {
    def query = params.q
    //def limit = params.limit !=null ? params.limit.asType(Integer.class) : 10
    def list = darwinCoreService.autoComplete(query, 10)
    render(contentType:"application/json") {list}
  }

}