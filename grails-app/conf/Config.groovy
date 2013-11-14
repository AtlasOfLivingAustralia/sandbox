/******************************************************************************\
 *  CONFIG MANAGEMENT
 \******************************************************************************/
def appName = 'datacheck'
def ENV_NAME = "${appName.toUpperCase()}_CONFIG"
def default_config = "/data/${appName}/config/${appName}-config.properties"
if(!grails.config.locations || !(grails.config.locations instanceof List)) {
    grails.config.locations = []
}

// add ala skin conf (needed for version >= 0.1.10)
if(System.getenv(ENV_NAME) && new File(System.getenv(ENV_NAME)).exists()) {
    println "[${appName}] Including configuration file specified in environment: " + System.getenv(ENV_NAME);
    grails.config.locations.add "file:" + System.getenv(ENV_NAME)
} else if(System.getProperty(ENV_NAME) && new File(System.getProperty(ENV_NAME)).exists()) {
    println "[${appName}] Including configuration file specified on command line: " + System.getProperty(ENV_NAME);
    grails.config.locations.add "file:" + System.getProperty(ENV_NAME)
} else if(new File(default_config).exists()) {
    println "[${appName}] Including default configuration file: " + default_config;
    grails.config.locations.add "file:" + default_config
} else {
    println "[${appName}] No external configuration file defined."
}

println "[${appName}] (*) grails.config.locations = ${grails.config.locations}"
appContext = 'datacheck'
appName = 'datacheck'
grails.project.groupId = "au.org.ala"
/******************************************************************************\
 *  EXTERNAL SERVERS
\******************************************************************************/
if(!grails.serverURL){
    grails.serverURL = "http://sandbox.ala.org.au/datacheck"
}
if(!sandboxHubsWebapp){
    sandboxHubsWebapp = "http://sandbox.ala.org.au/hubs-webapp"
}
if (!bie.baseURL) {
     bie.baseURL = "http://bie.ala.org.au/"
}
if (!biocache.baseURL) {
     biocache.baseURL = "http://biocache.ala.org.au/"
}
if (!spatial.baseURL) {
     spatial.baseURL = "http://spatial.ala.org.au/"
}
if (!ala.baseURL) {
    ala.baseURL = "http://www.ala.org.au"
}
if (!spatialPortalUrl) {
    spatialPortalUrl = "http://spatial.ala.org.au"
}
if(!spatialPortalUrlOptions){
    spatialPortalUrlOptions = "&dynamic=true&ws=http%3A%2F%2Fsandbox.ala.org.au%2Fhubs-webapp&bs=http%3A%2F%2Fsandbox.ala.org.au%2Fbiocache-service"
}
if(!biocacheServiceDownloadParams){
    biocacheServiceDownloadParams = "&extras=el882,el889,el887,el865,el894,cl21,cl22,cl927,cl23,cl617,cl620"
}
if(!biocacheServiceUrl){
    biocacheServiceUrl = "http://sandbox.ala.org.au/biocache-service"
}
if(!bie.searchPath){
    bie.searchPath = "/search"
}
if(!headerAndFooter.baseURL){
    headerAndFooter.baseURL = "http://www2.ala.org.au/commonui"
}
if(!uploadFilePath){
    uploadFilePath = "/data/datacheck/uploads/"
}

/******************************************************************************\
 *  SECURITY
\******************************************************************************/
if (!security.cas.urlPattern) {
    security.cas.urlPattern = ""
}
if (!security.cas.urlExclusionPattern) {
    security.cas.urlExclusionPattern = "/images.*,/css.*,/js.*"
}
if (!security.cas.authenticateOnlyIfLoggedInPattern) {
    security.cas.authenticateOnlyIfLoggedInPattern = ""
}
if (!security.cas.casServerName) {
    security.cas.casServerName = "https://auth.ala.org.au"
}
if (!security.cas.casServerUrlPrefix) {
    security.cas.casServerUrlPrefix = 'https://auth.ala.org.au/cas'
}
if (!security.cas.loginUrl) {
    security.cas.loginUrl = "${security.cas.casServerName}/cas/login"
}
if (!security.cas.logoutUrl) {
    security.cas.logoutUrl = "${security.cas.casServerName}/cas/logout"
}
if (!security.cas.contextPath) {
    security.cas.contextPath = "/datacheck" //"""${appName}"
}
if (!security.cas.bypass) {
    security.cas.bypass = false
}
if (!security.cas.appServerName ) {
    security.cas.appServerName = "http://localhost:8080"
}

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml'],
                      text: 'text/plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable for AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// set per-environment serverURL stem for creating absolute links
environments {
    production {

        grails.serverURL = "http://sandbox.ala.org.au/datacheck"
    }
    development {
        grails.serverURL = "http://localhost:8080/${appName}"
    }
    test {
        grails.serverURL = "http://localhost:8080/${appName}"
    }
}

// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    appenders {
        console name: "stdout", layout: pattern(conversionPattern: "%d %-5p [%c{1}]  %m%n"), threshold: org.apache.log4j.Level.ERROR
        environments {
            nectar {
                console name: "stdout", layout: pattern(conversionPattern: "%d %-5p [%c{1}]  %m%n"), threshold: org.apache.log4j.Level.ERROR
                rollingFile name: "tomcatLog", maxFileSize: 102400000, file: "/var/log/tomcat6/sandbox.log", threshold: org.apache.log4j.Level.ERROR, layout: pattern(conversionPattern: "%d %-5p [%c{1}] %m%n")
                'null' name: "stacktrace"
            }
            production {
                console name: "stdout", layout: pattern(conversionPattern: "%d %-5p [%c{1}]  %m%n"), threshold: org.apache.log4j.Level.ERROR
                rollingFile name: "tomcatLog", maxFileSize: 102400000, file: "/var/log/tomcat6/sandbox.log", threshold: org.apache.log4j.Level.ERROR, layout: pattern(conversionPattern: "%d %-5p [%c{1}] %m%n")
                'null' name: "stacktrace"
            }
            development {
                console name: "stdout", layout: pattern(conversionPattern: "%d %-5p [%c{1}]  %m%n"), threshold: org.apache.log4j.Level.DEBUG
                rollingFile name: "tomcatLog", maxFileSize: 102400000, file: "/tmp/sandbox.log", threshold: org.apache.log4j.Level.ERROR, layout: pattern(conversionPattern: "%d %-5p [%c{1}]  %m%n")
                'null' name: "stacktrace"
            }
            test {
                rollingFile name: "tomcatLog", maxFileSize: 102400000, file: "/tmp/sandbox.log", threshold: org.apache.log4j.Level.DEBUG, layout: pattern(conversionPattern: "%d %-5p [%c{1}]  %m%n")
                'null' name: "stacktrace"
            }
        }
    }

    root {
        // change the root logger to my tomcatLog file
        error 'tomcatLog'
        warn 'tomcatLog'
        info 'tomcatLog'
        debug 'tomcatLog', 'stdout'
        additivity = true
    }

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
            'org.codehaus.groovy.grails.web.pages', //  GSP
            'org.codehaus.groovy.grails.web.sitemesh', //  layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping', // URL mapping
            'org.codehaus.groovy.grails.commons', // core / classloading
            'org.codehaus.groovy.grails.plugins', // plugins
            'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
            'org.springframework',
            'org.hibernate',
            'net.sf.ehcache.hibernate',
            'org.codehaus.groovy.grails.plugins.orm.auditable',
            'org.mortbay.log',
            'org.springframework.webflow',
            'grails.app',
            'org.apache',
            'org',
            'com',
            'au',
            'grails.app',
            'net',
            'org.apache.http.wire',
            'http',
            'httpclient',
            'grails.spring.BeanBuilder',
            'grails.util.GrailsUtil',
            'org.apache.commons.http',
            'org.apache.commons.httpclient',
            'org.apache.commons.logging',
            'org.apache.commons.logging.simplelog.log.httpclient.wire'

    debug   'au.org.ala.datacheck',
            'grails.app.domain.au.org.ala.datacheck',
            'grails.app.controller.au.org.ala.datacheck',
            'grails.app.service.au.org.ala.datacheck',
            'grails.app.tagLib.au.org.ala.datacheck'
}