<script type="text/ng-template" id="datasets.html">
<!-- Container -->
<div class="container" id="main">
    <div class="row">
        <div class="col-sm-12">
            <h1>${grailsApplication.config.skin.appName}</h1>
        </div>
        <div class="col-sm-12">
            <div class="panel panel-default" ng-cloak>
                <div class="panel-heading">
                    <h2 class="panel-title" ng-bind="datasets.title()"></h2>
                </div>
                <div class="panel-body">
                    <div ng-show="datasets.userUploads">

                        <uib-alert ng-repeat="uid in datasets.deleteSuccesses" type="info" close="datasets.deleteSuccesses.splice($index,1)">Dataset {{ uid }} deleted</uib-alert>
                        <uib-alert ng-repeat="uid in datasets.deleteFailures" type="danger" close="datasets.deleteFailures.splice($index,1)">Unable to delete {{ uid }}.</uib-alert>

                        <div class="row">
                            <div class="col-sm-12">
                                <p>
                                    Here is a listing of {{ datasets.isCurrentUser() ? 'your' : datasets.user.displayName + "'s" }} previously uploaded datasets.<br/>
                                </p>
                                <button type="button" class="btn btn-primary" ui-sref="preview">Create new dataset</button>
                            </div>
                        </div>

                    </div>

                </div>
                <table class="table">
                    <thead>
                    <tr>
                        <th>Dataset ID</th>
                        <th>Dataset name</th>
                        <th>Date uploaded</th>
                        <th>Number of records</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tr ng-repeat="userUpload in datasets.userUploads">
                        <td ng-bind="userUpload.uid"></td>
                        <td ng-bind="userUpload.name"></td>
                        <td ng-bind="userUpload.dateCreated | date"></td>
                        <td ng-bind="userUpload.numberOfRecords"></td>
                        <td>
                            <a class="btn btn-default btn-sm"
                               ng-href="{{ userUpload.uiUrl || '${grailsApplication.config.sandboxHubsWebapp}/occurrences/search?q=data_resource_uid:' + userUpload.uid}}"><i
                                    class="fa fa-th-list"></i> View records</a>
                            <a class="btn btn-default btn-sm" ng-show="datasets.isCurrentUser()" ui-sref="chartOptions({tempUid: userUpload.uid})"><i class="fa fa-cog"></i> Configure charts</a>

                            <a class="btn btn-default btn-sm" ng-show="datasets.isCurrentUser()" ui-sref="preview({reload: userUpload.uid})"><i class="fa fa-repeat"></i> Reload</a>

                            <button class="btn btn-danger btn-sm" ng-show="datasets.isCurrentUser()" type="button" ng-click="datasets.deleteResource(userUpload.uid)"><i class="fa fa-trash"></i> Delete</button>

                        </td>
                    </tr>
                </table>
            </div>
        </div>
    </div>
</div><!-- End container #main col -->
</script>