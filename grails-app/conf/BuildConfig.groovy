grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.dependency.resolver = "maven"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

    repositories {
        mavenLocal()
        mavenRepo ("http://nexus.ala.org.au/content/groups/public/") {
            updatePolicy 'always'
        }
    }

    plugins {
        build ":release:3.0.1"
        build ":tomcat:7.0.54"
        runtime ":ala-web-theme:0.8.1"
        runtime ":csv:0.3.1"
        runtime ":tika-parser:1.3.0.1"
    }
    dependencies {
        runtime "commons-httpclient:commons-httpclient:3.1"
        runtime "commons-lang:commons-lang:2.6"
        runtime 'org.jsoup:jsoup:1.7.2'
        build "org.apache.tika:tika-parsers:1.4"
    }
}
