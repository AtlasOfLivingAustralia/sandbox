<script type="text/ng-template" id="allDatasets.html">
<div class="container" id="main">
    <div class="row">
        <div class="col-sm-12">
            <h1>${grailsApplication.config.skin.appName}<small>-  All datasets</small></h1>
            <div class="panel panel-default">
                <div class="panel-body">
                     <p>
                            Here is a listing of uploaded datasets in this system.
                     </p>
                </div>
                <table class="table">
                    <thead>
                    <tr>
                        <th>Dataset ID</th>
                        <th>Dataset name</th>
                        <th>Date uploaded</th>
                        <th>Uploaded by</th>
                        <th>Number of records</th>
                        <th></th>
                    </tr>
                    </thead>
                        <tr ng-repeat="userUpload in allDatasets.userUploads" ng-show="allDatasets.showUserUpload(userUpload)">
                            <td ng-bind="userUpload.uid"></td>
                            <td ng-bind="userUpload.name"></td>
                            <td ng-bind="userUpload.dateCreated | date"></td>
                            <td ng-bind="userUpload.userDisplayName"></td>
                            <td ng-bind="userUpload.numberOfRecords"></td>
                            <td>
                                <a class="btn btn-default btn-small"
                                   ng-href="{{ userUpload.uiUrl || '${grailsApplication.config.sandboxHubsWebapp}/occurrences/search?q=data_resource_uid:' + userUpload.uid }}"><i
                                        class="fa fa-th-list"></i> View records</a>
                            </td>
                        </tr>
                </table>
            </div>
        </div>
    </div>
</div>
</script>