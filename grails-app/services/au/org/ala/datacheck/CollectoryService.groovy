package au.org.ala.datacheck

import grails.converters.JSON
import groovy.json.JsonSlurper
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.PostMethod

class CollectoryService {

    static final String DEFAULT_API_KEY_HEADER = "api_key"

    def authService

    def grailsApplication

    def serviceMethod() {}

    /**
     * Retrieves a listing of uploads for this user.
     * @return JSON list of data resources
     */
    def getUserUploads(){
        try {
            def currentUserId = authService.getUserId()
            def url = "${grailsApplication.config.collectory.baseURL}/ws/tempDataResource?alaId=${currentUserId}"
            def js = new JsonSlurper()
            js.parseText(new URL(url).text)
        } catch (Exception e){
            log.error(e.getMessage(), e)
            null
        }
    }

    /**
     * Retrieves a listing of all uploads.
     * @return JSON list of data resources
     */
    def getAllUploads(){
        try {
            def url = "${grailsApplication.config.collectory.baseURL}/ws/tempDataResource"
            def js = new JsonSlurper()
            js.parseText(new URL(url).text)
        } catch (Exception e){
            log.error(e.getMessage(), e)
            null
        }
    }

    /**
     * Retrieves a listing of all uploads for this user.
     * @return JSON list of data resources
     */
    def getAllUploadsForUser(userId){
        try {
            def url = "${grailsApplication.config.collectory.baseURL}/ws/tempDataResource?alaId=${userId}"
            def js = new JsonSlurper()
            js.parseText(new URL(url).text)
        } catch (Exception e){
            log.error(e.getMessage(), e)
            null
        }

    }

    /**
     * Retrieves metadata for temporary resource
     * @param dataResourceUid
     * @return JSON metadata
     */
    def getTempResourceMetadata(dataResourceUid){
        try {
            def url = "${grailsApplication.config.collectory.baseURL}/ws/tempDataResource/${dataResourceUid}"
            def js = new JsonSlurper()
            js.parseText(new URL(url).text)
        } catch (Exception e){
            log.error(e.getMessage(), e)
            null
        }
    }

    Map saveTempDataResource(Map data, String uid) {
        if (uid) {
            String url = "${grailsApplication.config.collectory.baseURL}/ws/tempDataResource/${uid}"
            data[DEFAULT_API_KEY_HEADER] = grailsApplication.config.webservice.apiKey
            data.sourceFile = "${grailsApplication.config.grails.serverURL}/dataCheck/serveFile?uid=${uid}"
            data.remove('controller')
            data.remove('action')
            String body = (data as JSON).toString()

            def post = new PostMethod(url)
            def http = new HttpClient()
            log.debug("[areColumnHeaders] Sending: " + json)
            post.addRequestHeader("Content-Type", "application/json; charset=UTF-8")
            post.setRequestBody(body)
            http.executeMethod(post)


            JSON.parse(post.getResponseBodyAsString())
        }
    }
}
