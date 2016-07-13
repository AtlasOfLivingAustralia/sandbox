package au.org.ala.datacheck

import groovy.json.JsonSlurper

class MyDatasetsController {

    def collectoryService
    def biocacheService
    def authService
    def grailsApplication

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
                }
            }
        }
        [userUploads: collectoryService.getAllUploads()]
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
                        format: 'Pie chart',
                        visible: true
                ]
            }
        }

        def instance = [metadata: metadata, chartConfig: chartConfig, tempUid: params.tempUid]
        respond(instance, view:"charts", model: instance)
    }

    def saveChartOptions(){

        def chartOptions = []
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
        biocacheService.saveChartOptions(params.tempUid, chartOptions)
        redirect(action: 'index')
    }

    def deleteResource(){
        //check userId
        def currentUserId = authService.getUserId()
        def metadata = collectoryService.getTempResourceMetadata(params.uid)
        if(metadata.alaId == currentUserId){
            def success = biocacheService.deleteResource(params.uid)
            redirect(controller:"myDatasets", action:"index", params:[deleteSuccess:success])
        } else {
            redirect(controller:"myDatasets", action:"index", params:[deleteSuccess:false])
        }
        null
    }
}
