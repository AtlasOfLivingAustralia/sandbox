package au.org.ala.datacheck

import au.org.ala.datacheck.UploadService.UploadException
import au.org.ala.ws.service.WebService
import grails.converters.JSON
import org.springframework.web.multipart.MultipartFile

class ApiController {

    static allowedMethods = [ 'POST': 'uploadFile' ]

    def uploadService
    def apiKeyService

    def uploadFile() {

        response.setContentType("application/json")

        //API checks ?
        def apiKey = request.getHeader(WebService.DEFAULT_API_KEY_HEADER)

        def authenticated = false

        if (apiKey){
            log.info("API key provided in upload: " + apiKey)
            def apiKeyResponse = apiKeyService.checkApiKey(apiKey)
            authenticated = apiKeyResponse.valid
        } else {
            log.error("No API key provided in upload")
        }

        if (!authenticated){
            log.error("Unable to authenticated API key. Please check API key is provided and is valid")
            def result = [ error: true, message: "Unable to authenticated API key. Please check API key is provided and is valid" ]
            render result as JSON
        } else if (!request.metaClass.respondsTo(request, "getFileMap")) {
            log.error("Unable to find file in POST upload")
            def result = [ error: true, message: "Unable to find file in POST upload" ]
            render result as JSON
        } else {
            try {
                def files = request.getFileMap().values()

                if (!files){

                    log.error("Unable to find file in POST upload")
                    def result = [ error: true, message: "Unable to find file in POST upload" ]
                    render result as JSON

                } else {

                    MultipartFile file = files.iterator().next()
                    if (file) {
                        def uploadResult = uploadService.uploadFile("", file)
                        def sandboxUrl = grailsLinkGenerator.link(
                                absolute: true, controller: 'dataCheck', action: 'index', params:
                                [
                                    fileId: uploadResult.fileId,
                                    fileName: uploadResult.fileName,
                                    redirectToSandbox: null
                                ]
                        )
                        def result = uploadResult + [
                                dataResourceUid: params.uid, // blank for new resources
                                fileId: uploadResult.fileId,
                                location: sandboxUrl,
                                sandboxUrl: sandboxUrl,
                                error: false,
                                message: 'Successful upload'
                        ]
                        render result as JSON
                    } else {
                        log.error("Unable to find file in POST upload")
                        def result = [ error: true, message: "Unable to find file in POST upload" ]
                        render result as JSON
                    }
                }
            } catch (UploadException e) {
                log.error("Couldn't upload file because " + e.getMessage(), e)
                def result = [ error: true, message: e.message ]
                response.setStatus(400)
                render result as JSON
            } catch (Exception e) {
                log.error("Unexpected exception uploading file", e)
                def result = [ error: true, message: 'Unexpected error' ]
                response.setStatus(500)
                render result as JSON
            }
        }
    }
}
