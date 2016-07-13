class UrlMappings {

	static mappings = {
		"/api/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(view: "index")
		"/*"(view: "index")
		"/my-data"(view: "index")
		"/datasets/*"(view: "index")
		"/datasets/**/*"(view: "index")
		"/layers/*"(view: "index")
		"/layers/**/*"(view: "index")
		"/charts/*"(view: "index")
		"/charts/**/*"(view: "index")
		"/dataCheck/*" (view: "index")
		"/dataCheck/**/*" (view: "index")

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
