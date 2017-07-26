package au.org.ala.datacheck

import au.org.ala.datacheck.UploadService.UploadException
import org.springframework.web.multipart.MultipartFile

class ApiController {

    static allowedMethods = [ 'POST': 'uploadFile' ]

    def uploadService

    def uploadFile() {
        String dataResourceUid = params.uid
        MultipartFile file = request.getFile('file')
        try {
            def uploadResult = uploadService.uploadFile(dataResourceUid, file)
            def sandboxUrl = grailsLinkGenerator.link(absolute: true, controller: 'dataCheck', action: 'index', params: ['fileId': uploadResult.fileId, 'fileName': uploadResult.fileName, 'redirectToSandbox': null ])
            def result = uploadResult + [ sandboxUrl: sandboxUrl, error: false, message: '' ]
            respond result
        } catch (UploadException e) {
            log.error("Couldn't upload file because ", e)
            def result = [ error: true, message: e.message ]
            respond result, status: 400
        } catch (Exception e) {
            log.error("Unexpected exception uploading file", e)
            def result = [ error: true, message: 'unexpected error' ]
            respond result, status: 500
        }
    }

}
