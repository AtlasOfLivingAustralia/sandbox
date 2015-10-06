package au.org.ala.datacheck

class MyDatasetsController {

    def collectoryService
    def biocacheService
    def authService
    def grailsApplication

    def index(){
        def userUploads = collectoryService.getUserUploads()
        def currentUserId = authService.getUserId()
        //filter uploads by biocache URL
        def filteredUploads = userUploads.findAll { upload ->
            upload.webserviceUrl == grailsApplication.config.biocacheServiceUrl
        }
        [userUploads: filteredUploads, user:authService.getUserForUserId(currentUserId, true)]
    }

    def chartOptions(){
        //retrieve the current chart options
        //retrieve the list of custom indexes...
        def customIndexes = biocacheService.getCustomIndexes(params.tempUid)
        def chartConfig = biocacheService.getChartOptions(params.tempUid)

        if(!chartConfig){
            chartConfig = []
            customIndexes.each {
                chartConfig << [
                        field: it,
                        format: 'Pie chart',
                        visible: true
                ]
            }
        }

        render(view:"charts", model:[chartConfig : chartConfig, tempUid: params.tempUid])
    }

    def saveChartOptions(){

        def chartOptions = []
        def fields = params.field
        def format = params.format
        fields.eachWithIndex { field, idx ->

            def visibleFlag = 'visible_' + idx
            def values = params[visibleFlag]
            def visible = {
                if(values instanceof String[]){
                    true
                } else {
                    false
                }
            }.call()
            chartOptions << [
                field: field,
                format: format[idx],
                visible: visible
            ]
        }
        biocacheService.saveChartOptions(params.tempUid, chartOptions)
        redirect(action: 'index')
    }

    def deleteResource(){
        //check userId
        def currentUserId = authService.getUserId()
        def metadata = collectoryService.getTempResourceMetadata(params.uid)
        if(metadata.alaId == currentUserId){
            def success = biocacheService.deleteResource(params.uid)
            redirect(controller:"myDatasets", action:"index", params:[deleteSuccess:success])
        } else {
            redirect(controller:"myDatasets", action:"index", params:[deleteSuccess:false])
        }
        null
    }
}
