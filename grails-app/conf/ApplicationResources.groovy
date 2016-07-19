def dev = grails.util.GrailsUtil.isDevelopmentEnv()

modules = {
    application {
        dependsOn 'jquery', 'font-awesome', 'ala'
        // use these directly to exclude Bootstrap JS
//        resource url: grailsApplication.config.headerAndFooter.baseURL + '/css/bootstrap.min.css', attrs:[media:'screen, projection, print']
//        resource url: grailsApplication.config.headerAndFooter.baseURL + '/css/ala-styles.css', attrs:[media:'screen, projection, print']
        resource url: [dir: 'css', file: 'sandbox.css']
//        resource url: [dir: 'node_modules/jquery-ui/ui', file: 'core.js']
        resource url: [dir: 'node_modules/jquery-ui/ui', file: 'data.js']
        resource url: [dir: 'node_modules/jquery-ui/ui', file: 'scroll-parent.js']
        resource url: [dir: 'node_modules/jquery-ui/ui', file: 'widget.js']
        resource url: [dir: 'node_modules/jquery-ui/ui/widgets', file: 'mouse.js']
        resource url: [dir: 'node_modules/jquery-ui/ui/widgets', file: 'sortable.js']
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
        resource url: [dir: 'node_modules/angular-ui-sortable/dist', file: 'sortable.js']
        resource url: [dir: 'node_modules/angular-ui-router/release', file: 'angular-ui-router.js']
        resource url: [dir: 'node_modules/underscore', file: 'underscore.js']
        resource url: [dir: 'js', file: 'components.js' ]
        resource url: [dir: 'js', file: 'datasets.js' ]
        resource url: [dir: 'js', file: 'preview.js' ]
        resource url: [dir: 'js', file: 'application.js']
        resource url: [dir: 'js', file: 'app-routes.js' ]
        if (!dev) {
            resource url: [dir: 'js', file: 'app-production.js']
        }
    }
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
        resource url: [dir: 'js', file: 'preview-app.js']
        if (!dev) {
            resource url: [dir: 'js', file: 'app-production.js']
        }
    }
}

