package sandbox

import org.apache.commons.io.FileUtils

class BootStrap {

    def grailsApplication

    def init = { servletContext ->
        def directory = new File(grailsApplication.config.uploadFilePath)
        if(!directory.exists()){
            log.info "Creating upload directory..." + grailsApplication.config.uploadFilePath
            FileUtils.forceMkdir(directory)
        }
    }
    def destroy = {
    }
}
