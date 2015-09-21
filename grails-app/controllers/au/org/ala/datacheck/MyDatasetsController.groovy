package au.org.ala.datacheck

class MyDatasetsController {

    def collectoryService
    def biocacheService
    def authService
    def grailsApplication

    def index(){
        def userUploads = collectoryService.getUserUploads()
        def currentUserId = authService.getUserId()
        //filter uploads by biocache URL
        def filteredUploads = userUploads.collect { upload ->
            upload.webserviceUrl == grailsApplication.config.biocacheServiceUrl
        }
        [userUploads: filteredUploads, user:authService.getUserForUserId(currentUserId, true)]
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
