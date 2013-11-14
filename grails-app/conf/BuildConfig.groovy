grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        mavenLocal()
        mavenCentral()
        mavenRepo "http://snapshots.repository.codehaus.org"
        mavenRepo "http://repository.codehaus.org"
        mavenRepo "http://download.java.net/maven/2/"
        mavenRepo "http://repository.jboss.com/maven2/"
        mavenRepo "http://maven.ala.org.au/repository/"
    }
    plugins {
      //  build ":hibernate:$grailsVersion"
        build ":tomcat:$grailsVersion"        
        runtime ":ala-web-theme:0.1.10"
        runtime ":csv:0.3"
       // runtime ":standalone:1.0"
        //runtime ":svn:1.0.0.M1"
        runtime ":tika-parser:1.3.0.1"
    }
    dependencies {
        runtime 'org.jsoup:jsoup:1.7.2'
        // runtime 'net.sf.jsignature.io-tools:wazformat:1.2.12'
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        // runtime 'mysql:mysql-connector-java:5.1.13'
        build "org.apache.tika:tika-parsers:1.4"
    }
}
