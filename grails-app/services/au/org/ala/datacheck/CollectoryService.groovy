package au.org.ala.datacheck

import grails.transaction.Transactional
import groovy.json.JsonSlurper

class CollectoryService {

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
            def url = "${grailsApplication.config.collectoryUrl}/tempDataResource?alaId=${currentUserId}"
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
            def url = "${grailsApplication.config.collectoryUrl}/tempDataResource"
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
            def url = "${grailsApplication.config.collectoryUrl}/tempDataResource?alaId=${userId}"
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
            def url = "${grailsApplication.config.collectoryUrl}/tempDataResource/${dataResourceUid}"
            def js = new JsonSlurper()
            js.parseText(new URL(url).text)
        } catch (Exception e){
            log.error(e.getMessage(), e)
            null
        }
    }
}
