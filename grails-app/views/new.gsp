<%@ page contentType="text/html;charset=UTF-8" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>${grailsApplication.config.skin.appName} | ${grailsApplication.config.skin.orgNameLong}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="fluidLayout" content="true"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}-angular"/>
</head>

<body>
<div ui-view>

</div>

<script type="text/ng-template" id="preview.html">
<div class="col-sm-12 col-md-12" ng-cloak>

    <div class="panel panel-default">
        <div class="panel-heading">
            <div class="pull-right">
                <a ui-sref="/myUploads" class="btn btn-default">My uploaded datasets</a>
            </div>

            <h1>${grailsApplication.config.skin.appName}</h1>

            <h2>1. Load data here</h2>
        </div>

        <div class="panel-body">
            <p>
                This is a sandbox environment for data uploads, to allow users to view their data with mapping & analysis tools.
            </p>

            <p>
                This tool accepts comma separated values (CSV) and tab separated data.
            </p>

            <div id="initialPaste">
                <uib-tabset>
                    <uib-tab heading="Paste CSV">
                        <p>To paste your data, click the rectangle below, and type <kbd><kbd>ctrl</kbd> + <kbd>V</kbd></kbd> (Windows)
                        or <kbd><kbd>⌘</kbd> + <kbd>V</kbd></kbd> (OS X). For a large amount of data, this may take a while to parse.
                        </p>

                        <div class="form">
                            <div class="form-group">
                                <textarea id="copyPasteData" class="form-control" name="copyPasteData" rows="10"
                                          cols="120" ng-model="preview.text"
                                          ng-model-options="{ debounce: { 'default': 1500, 'blur': 0 } }"></textarea>
                            </div>

                            <button type="button" id="checkDataButton" class="btn btn-primary"
                                    ng-click="preview.parseColumns()" ng-disabled="preview.parsing"
                                    ng-bind="preview.checkDataLabel()"></button>
                        </div>
                    </uib-tab>
                    <uib-tab heading="Load File">
                        <p>
                            Upload a file here.
                        </p>

                        <div class="form">
                            <label class="btn btn-default btn-file">
                                {{preview.file.name || 'Select File' }} <input type="file" ngf-select ng-model="preview.file" ng-disabled="preview.parsing" style="display: none;">
                            </label>
                            <button type="button" class="btn btn-success" ng-show="preview.file" ng-disabled="preview.parsing"
                                    ng-click="preview.uploadCsvFile()" ng-bind="preview.uploadCsvStatusLabel()"></button>
                        </div>

                    </uib-tab>
                </uib-tabset>

                <p id="processingInfo"></p>
            </div><!-- initialPaste -->
        </div><!-- panel body -->
    </div><!-- panel -->
    <div class="panel panel-default" id="recognisedDataDiv" ng-show="preview.previewLoaded">
        <div class="panel-heading">
            <h2>2. Check our initial interpretation</h2>
        </div>

        <div class="panel-body">
            <p>Adjust headings that have been incorrectly matched using the text boxes.
            Fields marked in <strong>yellow</strong> havent been matched to a recognised field name
            (<a href="http://rs.tdwg.org/dwc/terms/" target="_blank">darwin core terms</a>).<br/>

                After adjusting, click
                <button type="button" class="btn btn-default" ng-disabled="preview.processingData"
                        ng-click="preview.reprocessData()">Reprocess sample</button>
            </p>

            <div class="well">
                <div id="recognisedData">
                    <div id="interpretation">
                        <label for="firstLineIsData">The first line is:</label>
                        <select id="firstLineIsData" name="firstLineIsData" ng-model="preview.preview.firstLineIsData"
                                ng-change="preview.parseColumns()" ng-options="o.value as o.label for o in preview.firstLineOptions">
                        </select>
                    </div>

                    <div id="tabulatedData" style="overflow: auto;">
                        %{--<h1></h1>--}%
                        <table id="initialParse" class="table table-bordered">
                            <thead>
                                <tr>
                                    <th ng-repeat="header in preview.preview.headers">
                                        <input class="columnHeaderInput" type="text" autocomplete="off" name="q"
                                            ng-model="header.header" ng-class="{unrecognizedField: !header.known}" />
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr ng-repeat="row in preview.preview.dataRows track by $index">
                                    <td ng-repeat="value in row track by $index" class="scrollCell"><div ng-bind="value"></div></td>
                                </tr>
                            </tbody>
                        </table>

                        <div id="jsonBlob" style="visibility: hidden; display:none;">
                            <pre id="columnHeaders"
                                      ng-bind="preview.preview.headers | json"></pre>
                        </div>
                    </div>
                </div>
                <button type="button" class="btn btn-default" ng-disabled="preview.processingData"
                        ng-click="preview.reprocessData()">Reprocess sample</button>
            </div>
        </div><!-- panel-body -->
    </div><!-- recognisedDataDiv -->

    <div class="panel panel-default" id="processSample" ng-show="preview.previewLoaded">
        <div class="panel-heading">
            <h2>3. Process sample & upload to sandbox</h2>
        </div>

        <div class="panel-body">
            <div id="processSampleUpload">
                <p style="display:none;">
                    <label for="customIndexedFields">Custom index fields</label>
                    <input type="text" name="customIndexedFields" id="customIndexedFields" value=""/>
                </p>

                <div class="bs-callout bs-callout-info">
                    The tables below display the first few records and our interpretation. The <strong>Processed value</strong>
                    displays the results of name matching, sensitive data lookup and reverse geocoding where coordinates have been supplied.<br/>
                    If you are happy with the initial processing, please give your dataset a name, and upload into the sandbox.
                    This will process all the records and allow you to visualise your data on a map.
                </div>

                <div class="well">
                    <label for="datasetName" class="datasetName"><strong>Your dataset name</strong></label>
                    <input id="datasetName" class="datasetName" name="datasetName" type="text"
                           value="My test dataset" style="width:350px; margin-bottom:5px;"/>
                    <button type="button" id="uploadButton" class="btn btn-primary"
                            ng-click="preview.uploadToSandbox()">Upload your data</button>

                    <div id="uploadFeedback" style="clear:right;" ng-show="preview.uploading || preview.uploadFailed">
                        <p ng-show="preview.uploadPercent == 0 && !preview.uploadFailed" class="uploaded">Starting upload of dataset....</p>
                        <span ng-show="preview.uploadPercent > 0 && preview.uploading < 100 && !preview.uploadFailed">Percentage completed: {{ preview.uploadPercent }}%. </span><span>STATUS: {{preview.uploadStatus}}, {{preview.uploadDescription}</span>
                        <span ng-show="preview.uploadFailed">Dataset upload <strong>failed</strong>. Please email <a href="mailto:${grailsApplication.config.skin.supportEmail}">${grailsApplication.config.skin.supportEmail}</a> with the details of your dataset.</span>
                    </div>

                    <uib-progressbar ng-show="preview.uploading" value="preview.uploadPercent" type="success" title="Upload progress"></uib-progressbar>

                    <div id="optionsAfterDownload" ng-show="preview.uploadPercent == 100"
                         style="margin-bottom: 0; padding-bottom: 0;">

                        <h2 style="margin-top:25px;">Options for working with your data</h2>

                        <div class="row">
                            <div class="col-sm-12">
                                <a ng-href="${createLink(controller: 'dataCheck', action: 'redirectToSpatialPortal')}?uid={{preview.dataResourceID}}" id="spatialPortalLink" class="btn btn-default"
                                   title="Mapping &amp; Analysis in the Spatial portal">Mapping & Analysis with your data</a>
                                <a ng-href="${createLink(controller: 'dataCheck', action: 'redirectToBiocache')}?uid={{preview.dataResourceID}}" id="hubLink" class="btn btn-default"
                                   title="Tables &amp; charts for your data">Tables & charts of your data</a>
                                <a ng-href="${createLink(controller: 'dataCheck', action: 'redirectToDownload')}?uid={{preview.dataResourceID}}" id="downloadLink" class="btn btn-default"
                                   title="Life Science Identifier (pop-up)">Download the processed version of your data</a>
                            </div>
                        </div>
                    </div>
                </div><!--well-->
            </div><!-- processSampleUpload-->
        </div><!-- panel-body -->
    </div><!-- processedSample -->

    <div id="processedData" ng-show="preview.processedData">
        <table ng-repeat="processedRecord in preview.processedData.processedRecords" id="processedSampleTable" class="table table-bordered" >
            <thead>
                <tr>
                    <th class="fieldNameTD">Field name</th>
                    <th>Original value</th>
                    <th>Processed value</th>
                </tr>
            </thead>
            <tbody>
                <tr ng-repeat="field in processedRecord.values">
                    <td class="fieldNameTD" ng-class="preview.processedRecordFieldClass(field)" ng-bind="field.name">
                        %{--TODO <g:prettyCamel value="${field.name}"/> --}%
                    </td>
                    <td ng-bind="field.raw"></td>
                    <td ng-class="preview.processedRecordChangedClass(field)" ng-bind="field.processed">
                        %{-- TODO <dc:formatProperty value="${field.processed}"/>--}%
                    </td>
                </tr>
                <tr ng-show="processedRecord.assertions" class="error">
                    <td colspan="3" class="error XXassertionHeader">
                        <span class="dataQualityHdr">Data quality tests for this record</span>
                        <span class="label label-success">Passed {{ preview.countByQaStatus(1) }}</span>
                        <span class="label label-warning">Warnings {{ preview.countByQaStatus(0) }}</span>
                    </td>
                </tr>
                <tr ng-repeat="assertion in processedRecord.assertions">
                    <td class="assertionName" ng-bind="assertion.name">
                        %{--TODO <g:message code="${assertion.name}" default="${assertion.name}"/>--}%
                    </td>
                    <td colspan="2">
                        <span class="label"
                              ng-class="'label-' + (assertion.qaStatus == 0 ? 'primary' : assertion.qaStatus == 1 ? 'success' : assertion.qaStatus == 2 ? 'warning' : 'default')"
                              ng-bind="assertion.qaStatus == 0 ? 'warning' : assertion.qaStatus == 1 ? 'passed' : assertion.qaStatus == 2 ? 'not checked' : ''"></span>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

    <div id="jb2" style="visibility: hidden; display:none;">
        <pre ng-bind="preview.preview.headers | json"></pre>
        <pre ng-bind="preview.cachedData | json"></pre>
    </div>

</div>
</script>

<script type="text/ng-template" id="dataset.html">
<div class="panel panel-default">
    <div class="panel-heading">
        <div class="well pull-right col-sm-3">
            <strong>Tip:</strong> To share your list of datasets with other users, use
            <a ng-href="${g.createLink(controller: "myDatasets", action: "userDatasets")}?userId={{dataset.userId}}">this link</a>.
        </div>

        <h1>${grailsApplication.config.skin.appName} - My uploaded datasets</h1>
    </div>

    <div class="panel-body">
        <div ng-show="dataset.userUploads">

            <uib-alert ng-repeat="deleteSuccess in datset.deleteSuccesses" type="info" close="dataset.deleteAlertDimissed()">Dataset deleted</uib-alert>
            <uib-alert ng-repeat="deleteFailure in datset.deleteFailures" type="error" close="dataset.deleteAlertDimissed()">Unable to delete this dataset.</uib-alert>
            <p class="lead">
                Here is a listing of your previously uploaded datasets.<br/>
            </p>

            <div class="row">
                <div class="col-sm-12">
                    <table class="table">
                        <thead>
                        <tr>
                            <th>Dataset name</th>
                            <th>Date uploaded</th>
                            <th>Number of records</th>
                            <th></th>
                        </tr>
                        </thead>
                            <tr ng-repeat="userUpload in dataset.userUploads">
                                <td ng-bind="userUpload.name"></td>
                                <td ng-bind="userUpload.dateCreated | date"></td>
                                <td ng-bind="userUpload.numberOfRecords"></td>
                                <td>
                                    <a class="btn btn-default"
                                       ng-href="userUpload.uiUrl || ${grailsApplication.config.sandboxHubsWebapp}/occurrences/search?q=data_resource_uid:{{userUpload.uid}}"><i
                                            class="fa fa-th-list"></i> View records</a>
                                    <a class="btn btn-default" ui-sref="datasets.chartOptions({tempUid: userUpload.uid})"><i class="fa fa-cog"></i> Configure charts</a>

                                    <a class="btn btn-default" ui-sref="datasets.reload({dataResourceUid: userUpload.uid})"><i class="fa fa-repeat"></i> Reload</a>

                                    <button class="btn btn-danger" type="button" ng-click="dataset.deleteResource(userUpload.uid)">Delete</button>

                                </td>
                            </tr>
                    </table>
                </div>
            </div>

        </div>

        <p class="lead" ng-show="!dataset.userUploads">
            You currently have no uploaded datasets. <g:link uri="/">Click here</g:link> to upload data.</p>
    </div>
</div>
</script>
<g:set var="loginLink"
       value="${grailsApplication.config.casServerLoginUrl}?service=${createLink(action: "index", controller: "dataCheck", absolute: true)}"/>

<g:render template="/alertModals" var="${loginLink}"/>

</body>
</html>