<%@ page contentType="text/html;charset=UTF-8" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>Sandbox file upload |  ${grailsApplication.config.skin.orgNameLong}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="${grailsApplication.config.skin.layout}" />
    <r:require modules="fileupload, progressbar, sandboxupload"/>
    <r:script disposition='head'>

      if (!window.console) console = {log: function() {}};

      var SANDBOXUPLOAD = {
        fileId:  "${params.id}",
        uploadStatusUrl: "${createLink(controller:'dataCheck', action:'uploadStatus')}?uid=",
        spatialPortalLink: "${createLink(controller:'dataCheck', action:'redirectToSpatialPortal')}?uid=",
        hubLink: "${createLink(controller:'dataCheck', action:'redirectToBiocache')}?uid=",
        downloadLink: "${createLink(controller:'dataCheck', action:'redirectToDownload')}?uid=",
        uploadLink: "${createLink(controller:'upload', action:'uploadToSandbox')}",
        parseColumnsWithFirstLineInfoUrl: "${createLink(controller:'upload', action:'parseColumnsWithFirstLineInfo')}?id=${params.id}&firstLineIsData=",
        viewProcessDataUrl:"${createLink(controller:'upload', action:'viewProcessData')}?id=",
        parseColumnsUrl:"${createLink(controller:'upload', action:'parseColumns')}?id=",
        userId: "${userId}"
      };

      //setup the page
      $(document).ready(function(){
           loadUploadedData('${params.id}');
           init();
      });
    </r:script>
</head>

<body>
    <div class="container">

      <g:if test="${params.dataResourceUid}">
          <h1>Dataset reload : preview</h1>
      </g:if>
      <g:else>
          <h1>Sandbox file upload</h1>
      </g:else>

      <div class="row-fluid" class="span12">

          <h2>1. File uploaded</h2>
          <div id="uploadedFileDetails" class="well">
            <h4><span>File name: </span><span id="filename">${params.fn}</span></h4>
          </div>

          <div id="recognisedDataDiv">
            <h2>2. Check our initial interpretation</h2>

            <p>
                Adjust headings that have been incorrectly matched using the text boxes.
                Fields marked in <strong>yellow</strong> havent been matched to a recognised field name
                (<a href="http://rs.tdwg.org/dwc/terms/" target="_blank">darwin core terms</a>).<br/>
                After adjusting, click
                <a href="javascript:processedData();" id="processDataBtn" name="processDataBtn" class="btn processDataBtn">Reprocess sample</a>
            </p>

            <div class="well" >
                <div id="recognisedData"></div>
                <a href="javascript:processedData();" id="processDataBtn2" name="processDataBtn2" class="btn processDataBtn">Reprocess sample</a>
            </div>

          </div><!-- recognisedDataDiv -->

            <div id="processSample">
            <h2>3. Upload to sandbox</h2>

            <div id="processSampleUpload">
              <div class="bs-callout bs-callout-info" style="margin-bottom:15px;">
              The tables below display the first few records and our interpretation. The <strong>Processed value</strong>
              displays the results of name matching, sensitive data lookup and reverse geocoding where coordinates have
              been supplied.<br/>
              If you are happy with the initial processing, please give your dataset a name, and upload into the sandbox.
              This will process all the records and allow you to visualise your data on a map.
              </div>

              <div class="well">

                <label for="datasetName" class="datasetName"><strong>Your dataset name</strong></label>
                <input id="datasetName" class="datasetName" name="datasetName" type="text" value="${params.datasetName ?: params.fn}"
                       style="width:350px; margin-bottom:5px;"/>

                <input id="dataResourceUid" name="dataResourceUid" type="hidden" value="${params.dataResourceUid}"/>

                <a id="uploadButton" class="btn btn-primary" href="javascript:uploadToSandbox();">Upload your data</a>

                <div id="uploadFeedback" style="clear:right;">
                </div>

                <div id="progressBar" class="progress progress-info" style="margin-top:20px;">
                    <div class="bar" data-percentage="0"></div>
                </div>

                <div id="optionsAfterDownload" style="display:none; margin-bottom: 0px; padding-bottom: 0px;">

                    <h2 style="margin-top:25px;">Options for working with your data</h2>

                    <div class="row-fluid">
                      <a href="#spatialPortalLink" id="spatialPortalLink" class="btn"
                         title="Mapping &amp; Analysis in the Spatial portal">Mapping & Analysis with your data</a></dd>
                      <a href="#hubLink" id="hubLink" class="btn" title="Tables &amp; charts for your data">
                          Tables & charts of your data</a></dd>
                      <a href="#downloadLink" id="downloadLink" class="btn" title="Life Science Identifier (pop-up)">
                          Download the processed version of your data</a></dd>
                    </div>
                </div>
              </div>
            </div>
          </div><!-- processedSample -->
      </div>
    </div>

<g:set var="loginLink" value="${grailsApplication.config.casServerLoginUrl}?service=${createLink(action: "preview", controller:"upload", absolute: true, params:[id: params.id, fn:params.fn])}"/>
    <g:render template="../alertModals" var="${loginLink}"/>
</body>
</html>