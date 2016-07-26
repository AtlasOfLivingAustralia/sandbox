package au.org.ala.datacheck

class DatasetsController {

    static allowedMethods = [deleteResource: 'POST']

    def authService
    def biocacheService
    def collectoryService

    def deleteResource() {
        def uid = params.uid
        def result
        def status = 200
        if (checkUserIsOwner(uid)) {
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
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'dataset.label', default: 'Dataset'), uid])
                // to delete data from biocache service, drt entry must be present in collectory. Once data is cleared delete drt.
                redirect(controller: 'tempDataResource', action: "delete" , params:[uid: params.uid])
            }
            '*' {
                log.debug("Responding")
                respond result, status: status
            }
        }

    }

    private def checkUserIsOwner(uid) {
        //check userId
        def currentUserId = authService.userId
        def metadata = collectoryService.getTempResourceMetadata(uid)
        return currentUserId == metadata.alaId
    }
}
