class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/my-data"(controller: "tempDataResource", action: "myData", plugin:'collectory-hub')
		"/jenkins/console/$jobName/$id/$start"(controller: 'jenkins', action: 'console')

//		"/layers/$tempUid"(controller: "myDatasets", action: "layers")
		"/"(controller: "dataCheck", action: 'index', view:"/dataCheck/index")
		"500"(view:'/error')
	}
}
