modules = {
    progressbar {
        resource url:[dir:'js', file:'bootstrap-progressbar.min.js']
        resource url:[dir:'css', file:'bootstrap-progressbar.min.css']
    }
    fileupload {
        resource url:[dir:'js', file:'bootstrap-fileupload.min.js']
        resource url:[dir:'css', file:'bootstrap-fileupload.min.css']
    }
    sandbox {
        resource url:[dir:'js', file:'sandbox.js']
    }
    sandboxupload {
        resource url:[dir:'js', file:'sandboxupload.js'], disposition: 'head'
    }
}

