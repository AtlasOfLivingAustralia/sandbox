<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head>
    <title>Sandbox | Atlas of Living Australia</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <script src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/jquery-ui.min.js"></script>
    <link href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css" rel="stylesheet" type="text/css"/>
    <style type="text/css">
      table { border-collapse: collapse; }
      th { font-size: 12px; border-collapse: collapse; border: 1px solid #000000; padding:2px; background-color: #000000; color: #ffffff;}
      td { font-size: 11px; border-collapse: collapse; border: 1px solid #000000; padding: 2px;}
      body { font-family: Arial; }
      .unrecognised { background-color: red; }
      .uploaded {font-size: 14px; font-weight: bold; }
      #copyPasteData { clear: both; display: block; width:90%; }
      .actionButton { clear: both; margin-top: 10px; }
      #content { width: 100%;  }
      #footer { width: 100%; padding:0px;}
      #footer-nav { width:95%;  padding: 25px 15px 0px 30px; }
      .copyright { width:95%; padding: 0px 15px 15px 30px; }
      .copyright  p { text-align:left; padding-top:0px; margin-top:0px;}
      h2#initialPaste { margin-top:15px;}
      #recognisedDataDiv, #processSample { margin-top:30px;}
      #tabulatedData { width:90%; overflow: auto; }

      #uploadFeedback { margin-top: 10px; margin-left: 10px; float:left; top: 0px; }
      .uploaded { padding-top: 20px;}
      #processedData { min-width: 650px; clear: both; }
      #uploadProgressBar { margin-top: 25px; }
      .datasetName {font-size: 14px;}

      #uploadProgressBar .ui-widget-header {
          border: 1px solid #C9AB67;
          background: #CEC193 url(/images/progress_1x100b.png) 50% 50% repeat-x;
      }

      #progress .ui-widget-content {
          border: 1px solid #C9AB67;
      }

    </style>
    <script type="text/javascript">

      function init(){
        $('#recognisedDataDiv').hide();
        $('#processSample').hide();
        $('#copyPasteData').val("");
        $('#processingInfo').hide();
        $('#uploadFeedback').html('');
      }

      function reset(){
         $('#recognisedDataDiv').hide();
         $('#processSample').hide();
         $('#processedContent').remove();
         $('#uploadFeedback').html('');
         $('#uploadProgressBar').html('');
         $('#uploadProgressBar').progressbar( "destroy" );
      }

      function parseColumns(){
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
        $.post("dataCheck/processData", { "firstLineIsData": $('#firstLineIsData').val(),
                  "headers": getColumnHeaders(),  "rawData": $('#copyPasteData').val() },
           function(data){
             $('#processedData').html(data)
           }, "html"
        );
      }

      var dataResourceUid = "";

      function updateStatus(uid){
        dataResourceUid = uid;
        updateStatusPolling();
      }

      function updateStatusPolling(){
        $.get("dataCheck/uploadStatus?uid="+dataResourceUid, function(data){
          if(data.status == "COMPLETE"){
            $('#uploadFeedback').html('<p class="uploaded">Dataset uploaded.&nbsp;&nbsp;<a href="http://sandbox.ala.org.au/hubs-webapp/occurrences/search?q=data_resource_uid:' + data.uid + '" target="_blank">Click here to view your data</a></span>.</p>');
            $("#uploadProgressBar").progressbar({ value: 100 });
          } else if(data.status == "FAILED"){
            $('#uploadFeedback').html('<p class="uploaded">Dataset upload <strong>failed</strong>. Please email support@ala.org.au with e details of your dataset.</p>');
          } else {
            $("#uploadProgressBar").progressbar({ value: data.percentage });
            $("#uploadFeedback").html('<p class="uploaded">'+data.status+', Completed: '+data.completed+', Percentage completed: '+data.percentage+'</p>');
            setTimeout("updateStatusPolling()", 2000);
          }
        });
      }

      function uploadToSandbox(){

        //$('#uploadFeedback').html('<p class="uploaded">Starting upload of dataset....</p>');
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
    </script>

  </head>

<body onload="javascript:init();">

<div id="content">
  <div id="wrapper" style="width:95%; padding:30px;">
  <h1>ALA Sandbox <span style="font-family: courier;">(Alpha)</span></h1>
  <p style="width:80%;">
    This is a sandbox environment for data uploads, to allow users to view their data with ALA tools.
    This is currently an experimental feature of the Atlas. Uploaded data will be periodically cleared from the system.
    This tool currently only accepts comma separated values (CSV) occurrence data and is currently limited to 1000 records per upload.
  </p>

  <div id="initialPaste">
    <h2>1. Paste your CSV data here</h2>
    <p>Please paste your data into the box below. To do this, click the rectangle below, and type control-V (Windows)
    or command-V (Macintosh). For a large amount of data, this may take a while to parse.
    </p>

    <g:textArea
            id="copyPasteData"
            name="copyPasteData" rows="15" cols="120"
            onkeyup="javascript:window.setTimeout('parseColumns()', 1000, true);"></g:textArea>
    <g:submitButton id="checkData" class="actionButton" name="checkData" value="Check Data"
        onclick="javascript:parseColumns();"/>
    <p id="processingInfo"></p>
  </div>

  <div id="recognisedDataDiv">
    <h2>2. Check our initial interpretation</h2>

    <p>Adjust headings that have been incorrectly matched using the text boxes.
    Fields marked in yellow havent been matched to a recognised field name.<br/>

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
    <div style="border: 1px dotted gray; background-color: #FAECBB; padding: 10px; margin-bottom: 10px; width:650px; clear:both; display:block;">
      <p>If you are happy with the initial processing, please give your dataset a name, and upload into the sandbox.</p>
      <p style="padding-bottom:0px;">
        <label for="datasetName" class="datasetName"><strong>Your dataset name</strong></label>
        <input id="datasetName" class="datasetName" name="datasetName" type="text" value="My test dataset" style="width:350px; margin-bottom:5px;"/>
        <input id="uploadButton" class="datasetName" type="button" value="Upload" onclick="javascript:uploadToSandbox();"/>


        <div id="uploadFeedback"></div>
        <div id="uploadProgressBar"></div>

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