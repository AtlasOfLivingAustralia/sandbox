package au.org.ala.datacheck

import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.HttpClient
import grails.converters.JSON
import org.apache.commons.httpclient.NameValuePair
import org.apache.commons.httpclient.methods.GetMethod
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import groovy.json.JsonOutput

class BiocacheService {

    static transactional = false

    def serviceMethod() {}

    def biocacheServiceUrl = ConfigurationHolder.config.biocacheServiceUrl

    def areColumnHeaders(String[] columnHeadersUnparsed){
      def post = new PostMethod(biocacheServiceUrl + "/parser/areDwcTerms")
      def http = new HttpClient()
      JsonOutput jsonOutput = new JsonOutput()
      def json = jsonOutput.toJson(columnHeadersUnparsed)
      log.debug("[areColumnHeaders] Sending: " + json)
   //   post.addRequestHeader("Content-Type", "application/json; charset=UTF-8")
      post.setRequestBody(json)
      http.executeMethod(post)
      Boolean.parseBoolean(post.getResponseBodyAsString())
    }

    def guessColumnHeaders(String[] columnHeadersUnparsed){
      def post = new PostMethod(biocacheServiceUrl + "/parser/matchTerms")
      def http = new HttpClient()
      JsonOutput jsonOutput = new JsonOutput()
      def json = jsonOutput.toJson(columnHeadersUnparsed)
    //  post.addRequestHeader("Content-Type", "application/json; charset=UTF-8")
      post.setRequestBody(json)
      http.executeMethod(post)
      def parseResponse = post.getResponseBodyAsString()  
      log.debug("Match terms response: " + parseResponse)
      JSON.parse(parseResponse)
    }

    def mapColumnHeaders(String[] columnHeadersUnparsed){
      def post = new PostMethod(biocacheServiceUrl + "/parser/mapTerms")
      def http = new HttpClient()
      JsonOutput jsonOutput = new JsonOutput()
      def json = jsonOutput.toJson(columnHeadersUnparsed)      
      log.debug("###### column headers to map : "  + json)
    //  post.addRequestHeader("Content-Type", "application/json; charset=UTF-8")
      post.setRequestBody(json)
      http.executeMethod(post)
      def map = JSON.parse(post.getResponseBodyAsString())
      def orderedMap = new LinkedHashMap<String,String>()
      columnHeadersUnparsed.each { key ->
           orderedMap.put(key, map.get(key))
      }
      orderedMap
    }

    def processRecord(String[] headers, String[] record){

      def post = new PostMethod(biocacheServiceUrl + "/process/adhoc")
      def http = new HttpClient()
      //construct the map
      def map = new LinkedHashMap()
      headers.eachWithIndex {header, idx ->  map.put(header, record[idx])}
      JsonOutput jsonOutput = new JsonOutput()
      def requestAsJSON = jsonOutput.toJson(map)      
      log.debug(requestAsJSON)
     // post.addRequestHeader("Content-Type", "application/json; charset=UTF-8")
      post.setRequestBody(requestAsJSON)
      http.executeMethod(post)

      //parse the result as a ParsedRecord
      def responseAsJSON = post.getResponseBodyAsString()
      log.debug(responseAsJSON)
      def json = JSON.parse(responseAsJSON)

      ParsedRecord parsedRecord = new ParsedRecord()

      List<ProcessedValue> processedValues = new ArrayList<ProcessedValue>()
      List<QualityAssertion> qualityAssertions = new ArrayList<QualityAssertion>()

      json?.values?.each { obj ->
        if(!obj.name.endsWith("ID") && obj.name != "left" && obj.name != "right"){
            ProcessedValue processedValue = new ProcessedValue()
            processedValue.name = obj.name
            processedValue.raw = obj.raw
            processedValue.processed = obj.processed
            processedValues.add(processedValue)
        }
      }

      json?.assertions?.each { obj ->
        QualityAssertion qa = new QualityAssertion()
        qa.code = obj.code
        qa.comment = obj.comment
        qa.name = obj.name
        qualityAssertions.add(qa)
      }

      parsedRecord.values = processedValues.toArray()
      parsedRecord.assertions = qualityAssertions.toArray()

      parsedRecord
    }

   /**
    * Upload the data to the biocache, passing back the response
    * @param csvData
    * @param headers
    * @param datasetName
    * @param separator
    * @param firstLineIsData
    * @return response as string
    */
    def uploadData(String csvData, String headers, String datasetName, String separator, String firstLineIsData, String customIndexedFields){

      //post.setRequestBody(([csvData:csvData, headers:headers]) as JSON)
      NameValuePair[] nameValuePairs = new NameValuePair[6]
      nameValuePairs[0] = new NameValuePair("csvData", csvData)
      nameValuePairs[1] = new NameValuePair("headers", headers)
      nameValuePairs[2] = new NameValuePair("datasetName", datasetName)
      nameValuePairs[3] = new NameValuePair("separator", separator)
      nameValuePairs[4] = new NameValuePair("firstLineIsData", firstLineIsData)
      nameValuePairs[5] = new NameValuePair("customIndexedFields", customIndexedFields)

      def post = new PostMethod(biocacheServiceUrl + "/upload/post")
      post.setRequestBody(nameValuePairs)

      def http = new HttpClient()
      http.executeMethod(post)

      //TODO check the response
      log.debug(post.getResponseBodyAsString())

      //reference the UID caches
      def get = new GetMethod(biocacheServiceUrl + "/cache/refresh")
      http.executeMethod(get)

      return post.getResponseBodyAsString()
    }

    def uploadStatus(String uid){
      def http = new HttpClient()
      def get = new GetMethod(biocacheServiceUrl + "/upload/status/"+uid+".json")
      http.executeMethod(get)
      return get.getResponseBodyAsString()
    }
}


