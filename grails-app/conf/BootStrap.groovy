import org.apache.commons.io.FileUtils

class BootStrap {

    def grailsApplication

    def init = { servletContext ->
        log.info "Creating upload directory..." + grailsApplication.config.uploadFilePath
        def directory = new File(grailsApplication.config.uploadFilePath)
        if(!directory.exists()){
            FileUtils.forceMkdir(directory)
        }
    }
    def destroy = {
    }
}
