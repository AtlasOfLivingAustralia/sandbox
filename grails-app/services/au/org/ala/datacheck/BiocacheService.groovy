package au.org.ala.datacheck

import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.HttpClient
import grails.converters.JSON

class BiocacheService {

    static transactional = false

    def serviceMethod() {}

    def biocacheServiceUrl = "http://ala-rufus.it.csiro.au:8080/biocache-service"

    def areColumnHeaders(String[] columnHeadersUnparsed){
      def post = new PostMethod(biocacheServiceUrl + "/parser/areDwcTerms")
      def http = new HttpClient()
      def json = columnHeadersUnparsed.encodeAsJSON()

      println("[areColumnHeaders] Sending: " + json)

      post.setRequestBody(json)
      http.executeMethod(post)
      Boolean.parseBoolean(post.getResponseBodyAsString())
    }

    def guessColumnHeaders(String[] columnHeadersUnparsed){
      def post = new PostMethod(biocacheServiceUrl + "/parser/matchTerms")
      def http = new HttpClient()
      def json = columnHeadersUnparsed.encodeAsJSON()
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
      post.setRequestBody(map.encodeAsJSON())
      http.executeMethod(post)

      //parse the result as a ParsedRecord
      def json = JSON.parse(post.getResponseBodyAsString())

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
}


