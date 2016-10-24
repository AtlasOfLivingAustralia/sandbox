/******************************************************************************\
 *  CONFIG MANAGEMENT
 \******************************************************************************/
def appName = 'sandbox'
def ENV_NAME = "${appName.toUpperCase()}_CONFIG"
default_config = "/data/${appName}/config/${appName}-config.properties"
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
grails.project.groupId = "au.org.ala"
/******************************************************************************\
 *  EXTERNAL SERVERS
\******************************************************************************/

ala.baseURL = "http://www.ala.org.au"

sandboxHubsWebapp = "http://sandbox.ala.org.au/ala-hub"

bie.baseURL = "http://bie.ala.org.au/"
biocache.baseURL = "http://biocache.ala.org.au/"
biocacheServiceDownloadParams = "&extras=el882,el889,el887,el865,el894,cl21,cl22,cl927,cl23,cl617,cl620"
biocacheServiceUrl = "http://sandbox.ala.org.au/biocache-service"

spatial.baseURL = "http://spatial.ala.org.au/"
spatialPortalUrl = "http://spatial.ala.org.au"
spatialPortalUrlOptions = "&dynamic=true&ws=http%3A%2F%2Fsandbox.ala.org.au%2Fhubs-webapp&bs=http%3A%2F%2Fsandbox.ala.org.au%2Fbiocache-service"


bie.searchPath = "/search"
uploadFilePath = "/data/${appName}/uploads/"


skin.orgNameLong = "Atlas of Living Australia"
skin.appName = "Sandbox"
skin.layout = "main"
apiKey = "xxxxxxxxxxxxx"

skin.supportEmail = "support@ala.org.au"

clubRole = ""
dataTypeToolTip = "Choose a data type: <b>text</b> (default), <b>integer</b> (whole numbers), <b>float</b> (number with decimal places) or <b>date</b> (ISO or Darwin Core accepted formats)"
dataTypeRegEx = '(.*)(_i|_d|_dt)$'

//grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = true
grails.mime.disable.accept.header.userAgents = []
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
grails.views.default.codec = "html" // none, html, base64
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

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/js/*', '/images/*', '/css/*', '/plugins/*', '/vendor/*', '/node_modules/*']
grails.resources.adhoc.includes = ['/js/**', '/images/**', '/css/**','/plugins/**', '/vendor/**', '/node_modules/**']

// make paginate tag compatible with BS3
grails.plugins.twitterbootstrap.fixtaglib = true

// set per-environment serverURL stem for creating absolute links
environments {
    production {

        grails.serverURL = "http://sandbox.ala.org.au/datacheck"
    }
    development {
        grails.serverURL = "http://localhost:8080/${appName}"
        grails.resources.debug = true
    }
    test {
        grails.serverURL = "http://localhost:8080/${appName}"
    }
}

grails.cache.config = {
    cache {
        name 'sandboxCache'
        eternal false
        overflowToDisk true
        maxElementsInMemory 10000
        maxElementsOnDisk 10000000
        timeToLiveSeconds (3600 * 12) //1 day
    }
}

def loggingDir = (System.getProperty('catalina.base') ? System.getProperty('catalina.base') + '/logs' : './logs')

// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    appenders {
        console name: "stdout", layout: pattern(conversionPattern: "%d %-5p [%c{1}]  %m%n"), threshold: org.apache.log4j.Level.ERROR
        environments {
            production {
                rollingFile name: "tomcatLog", maxFileSize: 102400000, file: "${loggingDir}/sandbox.log", threshold: org.apache.log4j.Level.INFO, layout: pattern(conversionPattern: "%d %-5p [%c{1}] %m%n")
                'null' name: "stacktrace"
            }
            development {
                console name: "stdout", layout: pattern(conversionPattern: "%d %-5p [%c{1}]  %m%n"), threshold: org.apache.log4j.Level.DEBUG
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
            'org.apache',
            'org',
            'com',
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

// Uncomment and edit the following lines to start using Grails encoding & escaping improvements

/* remove this line 
// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside null
                scriptlet = 'none' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        filteringCodecForContentType {
            //'text/html' = 'html'
        }
    }
}
remove this line */
