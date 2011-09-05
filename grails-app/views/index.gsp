<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head>
    <title>Data check</title>
    <script language="JavaScript" type="text/javascript" src="http://www.ala.org.au/wp-content/themes/ala/scripts/jquery-1.4.2.min.js"></script>
    <style type="text/css">
      table { border-collapse: collapse; }
      th { font-size: 12px; border-collapse: collapse; border: 1px solid #000000; padding:2px; background-color: #000000; color: #ffffff;}
      td { font-size: 11px; border-collapse: collapse; border: 1px solid #000000;}
      body { font-family: Arial; font-size: 11px;}
      .unrecognised { background-color: red; }
      .uploaded {font-size: 14px; font-weight: bold; }
      #copyPasteData { clear: both; display: block;}
      .actionButton { clear: both; margin-top: 10px; }
    </style>
  </head>
  <body>
    <script type="text/javascript">
      function parseColumns(){
        $.post("dataCheck/parseColumns", { "rawData": $('#copyPasteData').val() },
           function(data){
             $('#recognisedData').html(data)
           }, "html"
        );
      }

      function processedData(){
        $.post("dataCheck/processData", { "firstLineIsData": $('#firstLineIsData').val(), "headers": $('#columnHeaders').val(),  "rawData": $('#copyPasteData').val() },
           function(data){
             $('#processedData').html(data)
           }, "html"
        );
      }

      function uploadToSandbox(){
        $.post("dataCheck/upload",
            { "rawData": $('#copyPasteData').val(), "headers": $('#columnHeaders').val(),
              "datasetName": $('#datasetName').val(), "firstLineIsData": $('#firstLineIsData').val() },
            function(data){
              //alert('Value returned from service: '  + data.uid);
              $('#uploadFeedback').html('<p class="uploaded">Dataset uploaded. Temporary dataset UID  is : <a href="http://ala-rufus.it.csiro.au:8080/hubs-webapp/occurrences/search?q=data_resource_uid:' + data.uid + '">' + data.uid + '</span>.</p>');
            }
        );
      }
    </script>

    <div id="initialPaste">
      <h2>1. Paste your CSV data here</h2>
      <g:textArea id="copyPasteData" name="copyPasteData" rows="15" cols="120" onchange="javascript:parseColumns();" onkeyup="javascript:parseColumns();"></g:textArea>
      <g:submitButton id="checkData"  class="actionButton" name="checkData" value="Check Data" onclick="javascript:parseColumns();"/>
    </div>

    <div id="recognisedDataDiv">
      <h2>2. Recognised data</h2>
      <div id="recognisedData"></div>
      <g:submitButton id="processData" name="processData"  class="actionButton" value="Process Data" onclick="javascript:processedData();"/>
    </div>

    <div id="processSample">
      <h2>3. Process sample</h2>
      <form action="">
        <p>
          <label for="datasetName">Dataset name</label>
          <input id="datasetName" name="datasetName" type="text" value="" style="width:350px;">
          <br/>
          <input type="button" value="Upload" onclick="javascript:uploadToSandbox();"/>
          <span id="uploadFeedback"></span>
        </p>
      </form>
    </div>

    <div id="processedData"></div>
    <div id="jsonBlob" style="visibility: hidden; display:none;">
      <g:textArea id="columnHeaders" name="columnHeaders" cols="1000" rows="1000"><g:each in="${columnHeaders}" var="hdr">${hdr},</g:each></g:textArea>
      <g:textArea id="cachedData" name="cachedData" cols="1000" rows="1000"><g:each in="${dataRows}" var="row"><g:each in="${row}" var="value">${value},</g:each>
      </g:each></g:textArea>
    </div>
  </body>
</html>