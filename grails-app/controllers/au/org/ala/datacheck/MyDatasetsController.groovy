package au.org.ala.datacheck

import groovy.json.JsonSlurper

class MyDatasetsController {

    def collectoryService
    def biocacheService
    def authService
    def grailsApplication
    def formatService

    def index(){
        def userUploads = collectoryService.getUserUploads()
        def currentUserId = authService.getUserId()
        //filter uploads by biocache URL
        def filteredUploads = userUploads.findAll { upload ->
            upload.webserviceUrl == grailsApplication.config.biocacheServiceUrl
        }
        [userUploads: filteredUploads, user:authService.getUserForUserId(currentUserId, true), currentUserId: currentUserId]
    }

    def layers(){
        def metadata = collectoryService.getTempResourceMetadata(params.tempUid)
        def js = new JsonSlurper()
        def layers = js.parseText(new URL("http://spatial.ala.org.au/ws/layers").getText("UTF-8"))
        [layers: layers, metadata: metadata]
    }

    def userDatasets(){
        def instance = [userUploads: collectoryService.getAllUploadsForUser(params.userId), user: authService.getUserForUserId(params.userId, false)]
        respond instance, model: instance
    }

    def allDatasets(){

        def uploads = collectoryService.getAllUploads()
        uploads.each { upload ->
            if(upload.alaId) {
                def userDetails = authService.getUserForUserId(upload.alaId, false)
                if (userDetails) {
                    upload.userDisplayName = userDetails.displayName
                } else {
                    upload.userDisplayName = 'Unknown'
                }
            }
        }
        def instance = [userUploads: collectoryService.getAllUploads()]
        respond(instance, model: instance)
    }

    def chartOptions(){
        //retrieve the current chart options
        //retrieve the list of custom indexes...
        def metadata = collectoryService.getTempResourceMetadata(params.tempUid)
        def customIndexes = biocacheService.getCustomIndexes(params.tempUid)
        def chartConfig = biocacheService.getChartOptions(params.tempUid)

        if(!chartConfig){
            chartConfig = []
            customIndexes.each {
                chartConfig << [
                        field: it,
                        format: 'pie',
                        visible: true
                ]
            }
        }

        chartConfig.each { cfg ->
            cfg.formattedField = formatService.formatFieldName(cfg.field)
        }

        def instance = [metadata: metadata, chartConfig: chartConfig, tempUid: params.tempUid]
        respond(instance, view:"chartOptions", model: instance)
    }

    def saveChartOptions(){

        def chartOptions = []
        if (request.contentType?.startsWith('application/json')) {
            chartOptions = request.getJSON()
        } else { // form encoded
            def fields = params.field
            def format = params.format
            fields.eachWithIndex { field, idx ->

                def visibleFlag = 'visible_' + idx
                def values = params[visibleFlag]
                def visible = {
                    if(values instanceof String[]){
                        true
                    } else {
                        false
                    }
                }.call()
                chartOptions << [
                        field: field,
                        format: format[idx],
                        visible: visible
                ]
            }
        }
        def uid = params.tempUid
        log.debug("Saving chart options for $uid: $chartOptions")
        def status = [status: biocacheService.saveChartOptions(uid, chartOptions)]
        request.withFormat {
            form multipartForm {
//                flash.message = message(code: 'default.updated.message', args: [message(code: 'ChartOptions.label', default: 'Chart Options'), uid])
                redirect(action:"index", method: 'GET')
            }
            '*'{ respond status }
        }
    }

    def deleteResource() {
        def uid = params.uid
        def result
        def status = 200
        if(checkUserIsOwner(uid)) {
            log.debug("Attempting to delete $uid")
            def success = biocacheService.deleteResource(uid)
            log.debug("Delete $uid result: $success")
            result = [deleteSuccess: success]
        } else {
            log.warn("${authService.userId} attempting to delete $uid but is not the owner")
            status = 401
            result = [deleteSuccess: false]
        }
        request.withFormat {
            form multipartForm {
                log.debug("Redirecting")
//                flash.message = message(code: 'default.deleted.message', args: [message(code: 'Resource.label', default: 'Resource'), uid])
                redirect(controller:"myDatasets", action:"index", params: result, method: 'GET')
            }
            '*'{
                log.debug("Responding")
                respond result, status: status
            }
        }
    }

    private def checkUserIsOwner(uid) {
        //check userId
        def currentUserId = authService.getUserId()
        def metadata = collectoryService.getTempResourceMetadata(uid)
        return currentUserId == metadata.alaId
    }
}
