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
    application {
        dependsOn 'jquery', 'font-awesome', 'ala'
        // use these directly to exclude Bootstrap JS
//        resource url: grailsApplication.config.headerAndFooter.baseURL + '/css/bootstrap.min.css', attrs:[media:'screen, projection, print']
//        resource url: grailsApplication.config.headerAndFooter.baseURL + '/css/ala-styles.css', attrs:[media:'screen, projection, print']
        resource url: [dir: 'css', file: 'sandbox.css']
        resource url: [dir: 'node_modules/angular', file: 'angular-csp.css']
        resource url: [dir: 'node_modules/angular-ui-bootstrap/dist', file: 'ui-bootstrap-csp.css']
        resource url: [dir: 'node_modules/angular-loading-bar/build', file: 'loading-bar.css']
        resource url: [dir: 'node_modules/angular', file: 'angular.js']
        resource url: [dir: 'node_modules/angular-animate', file: 'angular-animate.js']
        resource url: [dir: 'node_modules/angular-aria', file: 'angular-aria.js']
        resource url: [dir: 'node_modules/angular-touch', file: 'angular-touch.js']
        resource url: [dir: 'node_modules/angular-loading-bar/build', file: 'loading-bar.js']
        resource url: [dir: 'node_modules/ng-file-upload/dist', file: 'ng-file-upload.js']
        resource url: [dir: 'node_modules/angular-ui-bootstrap/dist', file: 'ui-bootstrap-tpls.js']
        resource url: [dir: 'node_modules/angular-ui-router/release', file: 'angular-ui-router.js']
        resource url: [dir: 'node_modules/underscore', file: 'underscore.js']
        resource url: [dir: 'js', file: 'application.js']
    }
}

