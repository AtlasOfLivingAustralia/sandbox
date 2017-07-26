package au.org.ala.datacheck

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?" {
            constraints {
                // apply constraints here
            }
        }

        "/my-data"(controller: "tempDataResource", action: "myData", plugin: 'collectory-hub')
        "/jenkins/console/$jobName/$id/$start"(controller: 'jenkins', action: 'console')

        "/"(controller: "dataCheck", action: 'index')

        "500"(view: '/error')
    }

}
