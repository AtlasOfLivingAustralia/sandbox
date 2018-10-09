grails.project.groupId = 'au.org.ala'

grails.serverURL = 'http://local.ala.org.au:8080'

ENV_NAME = "SANDBOX_CONFIG"
appName = 'sandbox'
grails.config.locations = ["file:/data/${appName}/config/${appName}-config.properties",
                           "file:/data/${appName}/config/${appName}-config.yml",
                           "file:/data/${appName}/config/${appName}-config.groovy"]
if (System.getenv(ENV_NAME) && new File(System.getenv(ENV_NAME)).exists()) {
    println "[EXPERT] Including configuration file specified in environment: " + System.getenv(ENV_NAME);
    grails.config.locations = ["file:" + System.getenv(ENV_NAME)]
} else if (System.getProperty(ENV_NAME) && new File(System.getProperty(ENV_NAME)).exists()) {
    println "[EXPERT] Including configuration file specified on command line: " + System.getProperty(ENV_NAME);
    grails.config.locations = ["file:" + System.getProperty(ENV_NAME)]
} else {
    println "[EXPERT] Including default configuration files, if they exist: " + grails.config.locations
}

grails.cors.enabled = true

ignoreCookie = 'true'
security {
    cas {
        // appServerName is automatically set from grails.serverURL

        uriFilterPattern = '/alaAdmin.*,/api/.*'
        uriExclusionFilterPattern = '/assets/.*,/images/.*,/css/.*,/js/.*,/less/.*,/api/.*'

        //authenticateOnlyIfLoggedInPattern requires authenticateOnlyIfLoggedInPattern to identify 'logged in' when ignoreCookie='true'
        authenticateOnlyIfLoggedInPattern = '.*'
    }
}

headerAndFooter.excludeApplicationJs = true
orgNameLong = 'Atlas of Living Australia'
breadcrumbParent = 'https://www.ala.org.au/explore-by-location/,Explore'

habitat.layerID = '918'
map.bounds = '[-44, 112, -9, 154]'

ala.baseURL = "http://www.ala.org.au"
bie.baseURL = "http://bie.ala.org.au"
biocacheService.baseURL = "https://sandbox.ala.org.au/ws"
biocache.baseURL = "https://sandbox.ala.org.au/ala-hub"

spatial.baseURL = "http://spatial.ala.org.au"
spatialPortalUrlOptions = "&dynamic=true&ws=http%3A%2F%2Fsandbox.ala.org.au%2Fala-hub&bs=http%3A%2F%2Fsandbox.ala.org.au%2Fbiocache-service"

download.offline = true

uploadFilePath = "/data/${appName}/uploads/"

skin.orgNameLong = "Atlas of Living Australia"
skin.appName = "Sandbox"
skin.layout = "main"
skin.supportEmail = "support@ala.org.au"

apiKey = "xxxxxxxxxxxxx"
google.apikey = 'xxxxxxxx'

clubRole = ""
dataTypeToolTip = "Choose a data type: <b>text</b> (default), <b>integer</b> (whole numbers), <b>float</b> (number with decimal places) or <b>date</b> (ISO or Darwin Core accepted formats)"
dataTypeRegEx = '(.*)(_i|_d|_dt)$'

collectory.baseURL = "http://collections.ala.org.au"
logger.baseURL = "http://logger.ala.org.au/service"
speciesList.baseURL = "http://lists.ala.org.au"

alertResourceName = "ALA"
collectory.resources = "https://collections.ala.org.au/public/resources.json"

// Disable UI components
disableOverviewMap = "true"
disableAlertLinks = "true"
disableLoggerLinks = "false"

adminRole = "ROLE_ADMIN"

sandbox.hideCharts = "false"

grails.cache.config = {

    defaults {
        eternal false
        overflowToDisk false
        maxElementsInMemory 10000
    }

    cache {
        name 'sandboxCache'
        maxElementsOnDisk 10000000
        timeToLiveSeconds(3600 * 12) //1 day
    }

    //collectory-hub plugin
    cache {
        name 'collectoryCache'
        timeToLiveSeconds(3600 * 4)
    }

    //collectory-hub plugin
    cache {
        name 'longTermCache'
        timeToLiveSeconds(3600 * 24)
    }
}
