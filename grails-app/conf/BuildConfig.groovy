grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.plugin.location.'collectory-hub' = '../collectory-hub'

grails.server.port.http = 8080

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
    dependencies {
        runtime "commons-httpclient:commons-httpclient:3.1"
        runtime "commons-lang:commons-lang:2.6"
        runtime 'org.jsoup:jsoup:1.7.2'
        build "org.apache.tika:tika-parsers:1.4"
        build "org.apache.httpcomponents:httpclient:4.3.3"
    }
    plugins {
        compile ":cache:1.1.8"
     //   compile ":cache-ehcache:1.0.0"
        build ":release:3.0.1"
        build ":tomcat:7.0.70"
        runtime ":ala-bootstrap3:1.6"
        runtime ":ala-auth:1.3.1"
        runtime ":csv:0.3.1"
        runtime ":tika-parser:1.3.0.1"
        runtime ":pretty-time:2.1.3.Final-1.0.1"
        compile ":cache:1.1.8"
//        compile ":collectory-hub:0.1-SNAPSHOT"

        runtime ':resources:1.2.14'
        if (Environment.current == Environment.PRODUCTION) {
            runtime ":zipped-resources:1.0.1"
            runtime ":cached-resources:1.1"
            compile ":cache-headers:1.1.7"
            runtime ":yui-minify-resources:0.1.5"
        }
    }
}
