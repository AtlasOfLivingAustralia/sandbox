<script type="text/javascript">
    var SANDBOX_CONFIG = {
        autocompleteColumnHeadersUrl: '${createLink(controller:'dataCheck', action:'autocomplete')}',
        biocacheServiceUrl: '${grailsApplication.config.biocacheServiceUrl}',
        chartOptionsUrl: '${createLink(controller:'myDatasets', action: 'chartOptions')}',
        deleteResourceUrl: '${createLink(controller:'myDatasets', action: 'deleteResource')}',
        getAllDatasetsUrl: '${createLink(controller:'myDatasets', action: 'allDatasets')}',
        getDatasetsUrl: '${createLink(controller:'myDatasets', action: 'userDatasets')}',
        keepaliveUrl: '${createLink(controller: 'dataCheck', action: 'ping')}',
        loginUrl: '${grailsApplication.config.casServerLoginUrl}?service=${createLink(uri: '/', absolute: true)}',
        parseColumnsUrl: '${createLink(controller:'dataCheck', action:'parseColumns')}',
        processDataUrl: '${createLink(controller:'dataCheck', action:'processData')}',
        reloadDataResourceUrl: '${createLink(controller:'dataCheck', action:'reload')}',
        saveChartOptionsUrl: '${createLink(controller:'myDatasets', action:'saveChartOptions')}',
        uploadCsvUrl: '${createLink(controller:'dataCheck', action:'uploadFile')}',
        uploadToSandboxUrl: '${createLink(controller:'dataCheck', action:'upload')}',
        uploadStatusUrl: '${createLink(controller:'dataCheck', action:'uploadStatus')}',
        userId: '${u.userId()}',
        roles: <u:roles />,
        dataTypeToolTip: '${raw(grailsApplication.config.dataTypeToolTip)}',
        dataTypeRegEx: new RegExp('${raw(grailsApplication.config.dataTypeRegEx)}')
    };
</script>