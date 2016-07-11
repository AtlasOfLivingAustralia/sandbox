class UrlMappings {

	static mappings = {
		"/api/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(view: "new")
		"/*"(view: "new")
		"/my-data"(view: "new")
		"/datasets/**/*"(view: "new")
		"/layers/**/*"(view: "new")
		"/charts/**/*"(view: "new")
		"/dataCheck/*" (view: "new")
		"/dataCheck/**/*" (view: "new")

//		"/upload"(controller: "upload", action: "index")
//		"/reload/$dataResourceUid"(controller: "upload", action: "reload")
//		"/my-data"(controller: "myDatasets")
//		"/datasets/"(controller: "myDatasets", action: "allDatasets")
//		"/layers/$tempUid"(controller: "myDatasets", action: "layers")
//		"/datasets/$userId"(controller: "myDatasets", action: "userDatasets")
//		"/charts/$tempUid"(controller: "myDatasets", action: "chartOptions")
//		"/"(controller: "dataCheck", view:"/dataCheck/index")
		"500"(view:'/error')
	}
}
