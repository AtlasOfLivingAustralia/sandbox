def dev = grails.util.GrailsUtil.isDevelopmentEnv()

modules = {
    // cut down version of application for preview screen only
    preview {
        dependsOn 'ala'
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
        resource url: [dir: 'node_modules/underscore', file: 'underscore.js']
        resource url: [dir: 'js', file: 'components.js']
        resource url: [dir: 'js', file: 'preview.js']
        resource url: [dir: 'js', file: 'keepalive.js']
        resource url: [dir: 'js', file: 'preview-app.js']
        if (!dev) {
            resource url: [dir: 'js', file: 'app-production.js']
        }
    }
}

