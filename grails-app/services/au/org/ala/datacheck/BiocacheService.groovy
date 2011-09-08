package au.org.ala.datacheck

import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.HttpClient
import grails.converters.JSON
import org.apache.commons.httpclient.NameValuePair
import org.apache.commons.httpclient.methods.GetMethod

class BiocacheService {

    static transactional = false

    def serviceMethod() {}

    def biocacheServiceUrl = "http://sandbox.ala.org.au/biocache-service"

    def areColumnHeaders(String[] columnHeadersUnparsed){
      def post = new PostMethod(biocacheServiceUrl + "/parser/areDwcTerms")
      def http = new HttpClient()
      def json = columnHeadersUnparsed.encodeAsJSON()
      println("[areColumnHeaders] Sending: " + json)
      post.addRequestHeader("Content-Type", "application/json; charset=UTF-8")
      post.setRequestBody(json)
      http.executeMethod(post)
      Boolean.parseBoolean(post.getResponseBodyAsString())
    }

    def guessColumnHeaders(String[] columnHeadersUnparsed){
      def post = new PostMethod(biocacheServiceUrl + "/parser/matchTerms")
      def http = new HttpClient()
      def json = columnHeadersUnparsed.encodeAsJSON()
      post.addRequestHeader("Content-Type", "application/json; charset=UTF-8")
      post.setRequestBody(json)
      http.executeMethod(post)
      JSON.parse(post.getResponseBodyAsString())
    }

    def processRecord(String[] headers, String[] record){

      def post = new PostMethod(biocacheServiceUrl + "/process/adhoc")
      def http = new HttpClient()
      //construct the map
      def map = new HashMap()
      headers.eachWithIndex {header, idx ->  map.put(header, record[idx])}
      def requestAsJSON = map.encodeAsJSON()
      println(requestAsJSON)
      post.addRequestHeader("Content-Type", "application/json; charset=UTF-8")
      post.setRequestBody(requestAsJSON)
      http.executeMethod(post)

      //parse the result as a ParsedRecord
      def responseAsJSON = post.getResponseBodyAsString()
      println(responseAsJSON)
      def json = JSON.parse(responseAsJSON)

      ParsedRecord parsedRecord = new ParsedRecord()

      List<ProcessedValue> processedValues = new ArrayList<ProcessedValue>()
      List<QualityAssertion> qualityAssertions = new ArrayList<QualityAssertion>()

      json?.values?.each { obj ->
        ProcessedValue processedValue = new ProcessedValue()
        processedValue.name = obj.name
        processedValue.raw = obj.raw
        processedValue.processed = obj.processed
        processedValues.add(processedValue)
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
    *  Upload the data to the biocache, passing back the response
    * @param csvData
    * @param headers
    * @param datasetName
    * @param separator
    * @param firstLineIsData
    * @return
    */
    def uploadData(String csvData, String headers, String datasetName, String separator, String firstLineIsData){

      //post.setRequestBody(([csvData:csvData, headers:headers]) as JSON)
      NameValuePair[] nameValuePairs = new NameValuePair[5]
      nameValuePairs[0] = new NameValuePair("csvData", csvData)
      nameValuePairs[1] = new NameValuePair("headers", headers)
      nameValuePairs[2] = new NameValuePair("datasetName", datasetName)
      nameValuePairs[3] = new NameValuePair("separator", separator)
      nameValuePairs[4] = new NameValuePair("firstLineIsData", firstLineIsData)

      def post = new PostMethod(biocacheServiceUrl + "/upload/post")
      post.setRequestBody(nameValuePairs)

      def http = new HttpClient()
      http.executeMethod(post)

      //TODO check the response
      println(post.getResponseBodyAsString())

      //reference the UID caches
      def get = new GetMethod(biocacheServiceUrl + "/cache/refresh")
      http.executeMethod(get)

      return post.getResponseBodyAsString()
    }
}


