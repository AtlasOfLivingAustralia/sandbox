package au.org.ala.datacheck

import au.com.bytecode.opencsv.CSVReader
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.PostMethod

import org.apache.commons.httpclient.NameValuePair
import org.apache.commons.httpclient.methods.GetMethod

class DataCheckController {

  def biocacheService
  def noOfRowsToDisplay = 5

 def index = {
    redirect(uri:"../index")
  }

  def parseColumns = {

    //is it comma separated or tab separated
    def raw = request.getParameter("rawData").trim()
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
    String[] headers = request.getParameter("headers").split(",")
    def csvData = request.getParameter("rawData").trim()
    def firstLineIsData = Boolean.parseBoolean(request.getParameter("firstLineIsData"))

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

  def uploadStatus = {

    log.debug("Request to retrieve upload status")
    def responseString = biocacheService.uploadStatus(params.uid)
    response.setContentType("application/json")
    render(responseString)
  }

/*
  def processFull = {

    println("Attempting to download")
    String[] headers = request.getParameter("headers").split(",")
    //println("retrieved headers: " + headers)
    //retrieve the JSON string
    def csvData = request.getParameter("data")

    //send all the CSV to the process
    println("processing...")

    def fileId = System.currentTimeMillis()
    def fout = new FileOutputStream(new File("/tmp/qa-"+fileId+".csv"))

    AdHocParser.processToStream(headers, csvData, fout)

    fout.close()

    println("finished processing...file id: "+ fileId)

    render(view:'processedDownload',  model:[fileId:fileId])
  }

  def downloadFile = {
    String fileId = request.getParameter("fileId")
    response.setContentType("application/csv")
    response.setHeader("Cache-Control", "must-revalidate");
    response.setHeader("Pragma", "must-revalidate");
    response.setHeader("Content-Disposition", "attachment;filename=" + fileId +".csv");

    def fIn = new FileInputStream(new File("/tmp/qa-"+fileId+".csv"))

    def buff = new byte[1024]

    def output = response.getOutputStream()

    def bytesRead = 0

    while( (bytesRead = fIn.read(buff) )!= -1){
        output.write(buff, 0, bytesRead)
    }
    output.close()
  }
*/

}