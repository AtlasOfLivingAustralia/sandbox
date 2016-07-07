<%@ page contentType="text/html;charset=UTF-8" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>${grailsApplication.config.skin.appName} | ${grailsApplication.config.skin.orgNameLong}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <r:require modules="progressbar, sandbox"/>
    <r:script disposition='head'>

      if (!window.console) console = {log: function() {}};

      var SANDBOX = {
          dataResourceUid: "",
          currentPaste:"",
          userId: "${userId}"
      }

      //setup the page
      $(document).ready(function(){

          $('#copyPasteData').keyup( function(){
              window.setTimeout('parseColumns()', 1500, true);
          });

          init();

          //enable tabs in text areas
          $("textarea").keydown(function(e) {
              if(e.keyCode === 9) { // tab was pressed
                  // get caret position/selection
                  var start = this.selectionStart;
                  end = this.selectionEnd;
                  var $this = $(this);
                  // set textarea value to: text before caret + tab + text after caret
                  $this.val($this.val().substring(0, start)
                          + "\t"
                          + $this.val().substring(end));
                  // put caret at right position again
                  this.selectionStart = this.selectionEnd = start + 1;
                  // prevent the focus lose
                  return false;
              }
          });
      });
    </r:script>
</head>

<body>
<div class="panel panel-default">
    <div class="panel-heading">
        <div class="pull-right">
            <g:link controller="myDatasets" action="index" class="btn btn-default">My uploaded datasets</g:link>
        </div>

        <h1>${grailsApplication.config.skin.appName}</h1>
    </div>

    <div class="panel-body">

        <div class="row">
            <div class="col-sm-12">
                <p>
                    This is a sandbox environment for data uploads, to allow users to view their data with mapping & analysis tools.
                    <br/>
                    This tool accepts comma separated values (CSV) and tab separated data.
                    <br/>
                    To <b>upload file</b> instead, <g:link controller="upload" action="index">click here</g:link>.
                    <br/>
                </p>

                <div id="initialPaste">
                    <h2>1. Paste your CSV data here</h2>

                    <p>To paste your data, click the rectangle below, and type <strong>control-V (Windows)</strong>
                        or <strong>command-V (Macintosh)</strong>. For a large amount of data, this may take a while to parse.
                    </p>

                    <div class="form">
                        <div class="form-group">
                            <g:textArea
                                    id="copyPasteData"
                                    class="form-control"
                                    name="copyPasteData" rows="10" cols="120"></g:textArea>
                        </div>

                        <a href="javascript:parseColumns();" id="checkDataButton" class="btn btn-default">Check data</a>

                        <p id="processingInfo"></p>
                    </div>
                </div><!-- initialPaste -->

                <div id="recognisedDataDiv">
                    <h2>2. Check our initial interpretation</h2>

                    <p>Adjust headings that have been incorrectly matched using the text boxes.
                    Fields marked in <strong>yellow</strong> havent been matched to a recognised field name
                    (<a href="http://rs.tdwg.org/dwc/terms/" target="_blank">darwin core terms</a>).<br/>

                        After adjusting, click
                        <a href="javascript:processedData();" id="processData2" name="processData2"
                           class="btn btn-default processDataBtn">Reprocess sample</a></p>

                    <div class="well">
                        <div id="recognisedData"></div>
                        <a href="javascript:processedData();" id="processData" name="processData"
                           class="btn btn-default processDataBtn">Reprocess sample</a>
                    </div>
                </div><!-- recognisedDataDiv -->

                <div id="processSample">
                    <h2>3. Process sample & upload to sandbox</h2>

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
                            <a id="uploadButton" class="btn btn-primary"
                               href="javascript:uploadToSandbox();">Upload your data</a>

                            <div id="uploadFeedback" style="clear:right;">
                            </div>

                            <div class="progress" style="margin-top:20px;">
                                <div id="progressBar" class="progress-bar progress-bar-info" role="progressbar"
                                     data-transitiongoal="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%">
                                </div>
                            </div>

                            <div id="optionsAfterDownload"
                                 style="display:none; margin-bottom: 0px; padding-bottom: 0px;">

                                <h2 style="margin-top:25px;">Options for working with your data</h2>

                                <div class="row">
                                    <div class="col-sm-12">
                                        <a href="#spatialPortalLink" id="spatialPortalLink" class="btn"
                                           title="Mapping &amp; Analysis in the Spatial portal">Mapping & Analysis with your data</a>
                                        <a href="#hubLink" id="hubLink" class="btn"
                                           title="Tables &amp; charts for your data">Tables & charts of your data</a>
                                        <a href="#downloadLink" id="downloadLink" class="btn"
                                           title="Life Science Identifier (pop-up)">Download the processed version of your data</a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div><!-- processedSample -->

                <div id="processedData"></div>

                <div id="jsonBlob" style="visibility: hidden; display:none;">
                    <g:textArea id="columnHeaders" name="columnHeaders" cols="1000" rows="1000"><g:each
                            in="${columnHeaders}"
                            var="hdr">${hdr},</g:each></g:textArea>
                    <g:textArea id="cachedData" name="cachedData" cols="1000" rows="1000"><g:each in="${dataRows}"
                                                                                                  var="row"><g:each
                                in="${row}" var="value">${value},</g:each>
                    </g:each></g:textArea>
                </div>
            </div><!-- processedData -->
        </div> <!-- column -->
    </div><!-- row -->
</div><!-- panel-body -->
</div><!-- panel -->


<g:set var="loginLink"
       value="${grailsApplication.config.casServerLoginUrl}?service=${createLink(action: "index", controller: "dataCheck", absolute: true)}"/>

<g:render template="/alertModals" var="${loginLink}"/>

</body>
</html>