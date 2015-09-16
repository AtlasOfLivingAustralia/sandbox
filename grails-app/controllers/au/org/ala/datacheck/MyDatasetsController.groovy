package au.org.ala.datacheck

class MyDatasetsController {

    def collectoryService
    def biocacheService
    def authService

    def index(){
        def userUploads = collectoryService.getUserUploads()
        def currentUserId = authService.getUserId()
        [userUploads: userUploads, user:authService.getUserForUserId(currentUserId, true)]
    }

    def deleteResource(){
        def success = biocacheService.deleteResource(params.uid)
        redirect(controller:"myDatasets", action:"index")
        null
    }
}
