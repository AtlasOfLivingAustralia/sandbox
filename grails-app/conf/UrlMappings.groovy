class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/upload"(controller: "upload", action: "index")
		"/reload/$dataResourceUid"(controller: "upload", action: "reload")
		"/my-data"(controller: "myDatasets")

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
