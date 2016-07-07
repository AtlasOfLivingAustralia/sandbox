modules = {
    progressbar {
        resource url: [dir: 'vendor/bootstrap-progressbar-0.9.0', file: 'bootstrap-progressbar.js']
        resource url: [dir: 'vendor/bootstrap-progressbar-0.9.0/css', file: 'bootstrap-progressbar-3.3.4.css']
    }
    jasnybootstrap {
        resource url: [dir: 'vendor/jasny-bootstrap-3.1.3/js', file: 'jasny-bootstrap.js']
        resource url: [dir: 'vendor/jasny-bootstrap-3.1.3/css', file: 'jasny-bootstrap.css']
    }
    sandbox {
        resource url: [dir: 'js', file: 'sandbox.js']
        resource url: [dir: 'css', file: 'sandbox.css']
    }
    sandboxupload {
        resource url: [dir: 'js', file: 'sandboxupload.js'], disposition: 'head'
        resource url: [dir: 'css', file: 'sandbox.css']
    }
}

