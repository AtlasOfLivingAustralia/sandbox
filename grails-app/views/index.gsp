<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head>
    <title>Sandbox | Atlas of Living Australia</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="ala2" />
	<script language="JavaScript" type="text/javascript" src="http://www.ala.org.au/wp-content/themes/ala/scripts/jquery.autocomplete.js"></script>
    <link rel="stylesheet" type="text/css" media="screen" href="http://www.ala.org.au/wp-content/themes/ala/css/jquery.autocomplete.css" />
    <link href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css" rel="stylesheet" type="text/css"/>
    <uploader:head />
    <style type="text/css">

    </style>
    <script type="text/javascript">

      function init(){
        console.log("Initialising sandbox...");
        $('#recognisedDataDiv').hide();
        $('#processSample').hide();
        $('#copyPasteData').val("");
        $('#processingInfo').hide();
        $('#uploadFeedback').html('');
        $('#uploadProgressBar').hide();
        $('#uploadFeedback').hide();
      }

      function reset(){
          console.log("Reset sandbox...");
         $('#recognisedDataDiv').hide();
         $('#processSample').hide();
         $('#processedContent').remove();
         $('#uploadFeedback').html('');
         $('#uploadProgressBar').html('');
         $('#uploadProgressBar').progressbar( "destroy" );
         $('#uploadProgressBar').html('');
         $('#uploadProgressBar').hide();
         $('#uploadFeedback').hide();
      }

      function parseColumns(){
        console.log("Parsing columns, and resetting some state");
        if($('#copyPasteData').val().trim() == ""){
           reset();
        } else {
          $('#processingInfo').html('<strong>Parsing the pasted input.....</strong>');
          $('#uploadFeedback').html('');
          $('#uploadProgressBar').html('');
          $('#uploadProgressBar').progressbar( "destroy" );
          $.post("dataCheck/parseColumns", { "rawData": $('#copyPasteData').val() },
             function(data){
               $('#recognisedData').html(data)
               $('#recognisedDataDiv').show();
               processedData();
               $('#processSample').show();
               $('#processingInfo').html('<strong>&nbsp;</strong>');
               $('#firstLineIsData').change(function(){parseColumnsWithFirstLineInfo();});
             }, "html"
          );
        }
      }

      function parseColumnsWithFirstLineInfo(){
          console.log("Parsing first line to do interpretation...");
          $.post("dataCheck/parseColumnsWithFirstLineInfo", { "rawData": $('#copyPasteData').val(), "firstLineIsData": $('#firstLineIsData').val() },
             function(data){
               $('#recognisedData').html(data)
               $('#recognisedDataDiv').show();
               processedData();
               $('#processSample').show();
               $('#processingInfo').html('<strong>&nbsp;</strong>');
               $('#firstLineIsData').change(function(){parseColumnsWithFirstLineInfo();});
             }, "html"
          );
      }

      function processedData(){
        console.log("Process first few lines....");
        $.post("dataCheck/processData", {
              "firstLineIsData": $('#firstLineIsData').val(),
              "headers": getColumnHeaders(),
              "rawData": $('#copyPasteData').val()
           },
           function(data){
             $('#processedData').html(data)
           }, "html"
        );
      }

      var dataResourceUid = "";

      function updateStatus(uid){
        console.log('Uploading status for...' + uid);
        dataResourceUid = uid;
        $('#uploadProgressBar').show();
        $('#uploadFeedback').show();
        updateStatusPolling();
      }

      function updateStatusPolling(){

        $.get("dataCheck/uploadStatus?uid="+dataResourceUid, function(data){
          console.log("Retrieving status...." + data.status + ", percentage: " + data.percentage);
          if(data.status == "COMPLETE"){
            $("#uploadProgressBar").progressbar({ value: 100 });
            $('.ui-progressbar-value').html('<span>Dataset uploaded.&nbsp;&nbsp;<a href="dataCheck/redirectToBiocache?uid=' + dataResourceUid + '" target="_blank">Click here to view your data</a>.</span>');
            $("#uploadFeedback").html('');
          } else if(data.status == "FAILED"){
            $('.ui-progressbar-value').html('<span>Dataset upload <strong>failed</strong>. Please email support@ala.org.au with e details of your dataset.</span>');
          } else {
            $("#uploadProgressBar").progressbar({ value: data.percentage });
            $("#uploadFeedback").html('<span>STATUS: '+data.status+', Completed: '+data.completed+', Percentage completed: '+data.percentage+'</span>');
            setTimeout("updateStatusPolling()", 1000);
          }
        });
      }                                                                                 

      function uploadToSandbox(){
        console.log('Uploading to sandbox...');
        $('#uploadFeedback').html('<p class="uploaded">Starting upload of dataset....</p>');
        $.post("dataCheck/upload",
            { "rawData": $('#copyPasteData').val(), "headers": getColumnHeaders(),
              "datasetName": $('#datasetName').val(), "firstLineIsData": $('#firstLineIsData').val() },
            function(data){
              //alert('Value returned from service: '  + data.uid);
              updateStatus(data.uid);
            }
        );
      }

      function getColumnHeaders(){
        console.log("Retrieve column headers...");
        var columnHeaderInputs = $('input.columnHeaderInput');
        var columnHeadersCSV = "";
        var i=0;
        $.each(columnHeaderInputs, function(index, input){
          if(index>0){
           columnHeadersCSV = columnHeadersCSV + ",";
          }
          columnHeadersCSV = columnHeadersCSV + input.value;
          i++;
        });
        return columnHeadersCSV;
      }

      //setup the page
      $(document).ready(function() {
          javascript:init();
      });
    </script>

  </head>

<body>

<div id="content">
  <div id="wrapper" style="width:95%; padding:30px; text-align: left;">
  <h1>ALA Sandbox</h1>
  <p style="width:80%;">
    This is a sandbox environment for data uploads, to allow users to view their data with ALA tools.
    This is currently an <strong>experimental</strong> feature of the Atlas. Uploaded data will be <strong>periodically cleared</strong> from the system.
    This tool currently only accepts comma separated values (CSV) occurrence data and is currently <strong>limited to 1000 records</strong> per upload.
  </p>

  <div id="initialPaste">
    <h2>1. Paste your CSV data here</h2>
    <p>To paste your data, click the rectangle below, and type <strong>control-V (Windows)</strong>
    or <strong>command-V (Macintosh)</strong>. For a large amount of data, this may take a while to parse.
    </p>

    <g:textArea
        id="copyPasteData"
        name="copyPasteData" rows="15" cols="120"
        onkeyup="javascript:window.setTimeout('parseColumns()', 500, true);"></g:textArea>
    <g:submitButton id="checkData" class="actionButton" name="checkData" value="Check Data"
        onclick="javascript:parseColumns();"/>
    <p id="processingInfo"></p>

   <!-- <uploader:uploader id="yourUploaderId" /> -->

  </div>

  <div id="recognisedDataDiv">
    <h2>2. Check our initial interpretation</h2>

    <p>Adjust headings that have been incorrectly matched using the text boxes.
    Fields marked in <strong>yellow</strong> havent been matched to a recognised field name (<a href="http://rs.tdwg.org/dwc/terms/" target="_blank">darwin core terms</a>).<br/>

    After adjusting, click the
    <g:submitButton id="processData2" name="processData2" class="actionButton" value="Reprocess sample"
                    onclick="javascript:processedData();"/>
    button.</p>
    <div id="recognisedData"></div>
    <g:submitButton id="processData" name="processData" class="actionButton" value="Reprocess sample"
                    onclick="javascript:processedData();"/>
  </div>

  <div id="processSample">
    <h2>3. Process sample & upload to sandbox</h2>
    <div id="processSampleUpload">
      <p>
      The tables below display the first few records and our interpretation. The <strong>Processed value</strong>
      displays the results of name matching, sensitive data lookup and reverse geocoding where coordinates have been supplied.<br/>
      If you are happy with the initial processing, please give your dataset a name, and upload into the sandbox.
      This will process all the records and allow you to visualise your data on a map.
      </p>
      <p style="padding-bottom:0px;">
        <label for="datasetName" class="datasetName"><strong>Your dataset name</strong></label>
        <input id="datasetName" class="datasetName" name="datasetName" type="text" value="My test dataset" style="width:350px; margin-bottom:5px;"/>
        <input id="uploadButton" class="datasetName" type="button" value="Upload" onclick="javascript:uploadToSandbox();"/>
        <div id="uploadFeedback" style="clear:right;">
        </div>
        <div id="uploadProgressBar">
        </div>
      </p>
    </div>
  </div>

  <div id="processedData"></div>

  <div id="jsonBlob" style="visibility: hidden; display:none;">
    <g:textArea id="columnHeaders" name="columnHeaders" cols="1000" rows="1000"><g:each in="${columnHeaders}"
       var="hdr">${hdr},</g:each></g:textArea>
    <g:textArea id="cachedData" name="cachedData" cols="1000" rows="1000"><g:each in="${dataRows}" var="row"><g:each
            in="${row}" var="value">${value},</g:each>
    </g:each></g:textArea>
  </div>

  </div>

  </div>
</div>
</body>
</html>