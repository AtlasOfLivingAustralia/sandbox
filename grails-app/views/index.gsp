<%@ page contentType="text/html;charset=UTF-8" %>
<html xmlns="http://www.w3.org/1999/html">
  <head>
    <title>Sandbox | Atlas of Living Australia</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="ala3" />
    <uploader:head />
    <style type="text/css">

    </style>
    <script type="text/javascript">

      if (!window.console) console = {log: function() {}};

      var SANDBOX = {
          currentPaste: ""
      }

      function init(){
        console.log("Initialising sandbox...");
        $('#recognisedDataDiv').hide();
        $('#processSample').hide();
        $('#copyPasteData').val("");
        $('#processingInfo').hide();
        $('#uploadFeedback').html('');
        $('#uploadProgressBar').hide();
        $('#uploadFeedback').hide();
        if(typeof String.prototype.trim !== 'function') {
          String.prototype.trim = function() {
              return this.replace(/^\s+|\s+$/g, '');
          }
        }
      }

      function reset(){
         // console.log("Reset sandbox...");
         $('#processedContent').remove();
         $('#uploadFeedback').html('');
         $('#uploadProgressBar').html('');
         $('#uploadProgressBar').progressbar( "destroy" );
         $('#uploadProgressBar').html('');
          /*
          $('#recognisedDataDiv').hide();
          $('#processSample').hide();
          $('#uploadProgressBar').hide();
         $('#uploadFeedback').hide();
         */
          $('#recognisedDataDiv').hide();
          $('#processSample').slideUp("slow");
          $('#uploadProgressBar').hide();
          $('#uploadFeedback').hide();
      }

      function pasteAreaHasChanged(){
        if($('#copyPasteData').val().trim() == "" && SANDBOX.currentPaste == ""){
            return false;
        }
        if($('#copyPasteData').val().trim() !=  SANDBOX.currentPaste){
            SANDBOX.currentPaste = $('#copyPasteData').val().trim();
            return true;
        } else {
            return false;
        }
      }

      function parseColumns(){
       // console.log("Parsing columns, and resetting some state");
        if($('#copyPasteData').val().trim() == ""){
           reset();
        } else if(pasteAreaHasChanged()){

          $('#checkDataButton').html("Checking....");

          $('#processingInfo').html('<strong>Parsing the pasted input.....</strong>');
          $('#processedContent').html('');
          $('#uploadFeedback').html('');
          $('#uploadProgressBar').html('');
          $('#uploadProgressBar').progressbar( "destroy" );
          $.ajaxSetup({
            scriptCharset: "utf-8",
            contentType: "text/html; charset=utf-8"
          });
          $.ajax({
             type: "POST",
             url: "dataCheck/parseColumns",
             data: $('#copyPasteData').val(),
             success: function(data){
               console.log(data);
               $('#recognisedData').html(data)
               $('#recognisedDataDiv').slideDown("slow");
               processedData();
               $('#processSample').slideDown("slow");
               $('#processingInfo').html('<strong>&nbsp;</strong>');
               $('#firstLineIsData').change(function(){ parseColumnsWithFirstLineInfo(); });
               $('#checkDataButton').html("Check data");
             }
         });
        }
      }

      function parseColumnsWithFirstLineInfo(){
          console.log("Parsing first line to do interpretation...");
          $.post("dataCheck/parseColumnsWithFirstLineInfo", { "rawData": $('#copyPasteData').val(), "firstLineIsData": $('#firstLineIsData').val() },
             function(data){
               $('#recognisedData').html(data)
               $('#recognisedDataDiv').slideDown("slow");
               processedData();
               $('#processSample').slideDown("slow");
               $('#processingInfo').html('<strong>&nbsp;</strong>');
               $('#firstLineIsData').change(function(){parseColumnsWithFirstLineInfo();});
             }, "html"
          );
      }

      function processedData(){
//        console.log("Process first few lines...." + getColumnHeaders());
          $('#processedData').slideUp("slow");
         // $('#processedData').html("");
         // $('#processedData').html("<strong>Parsing the first three records...</strong>");
         // $('#processedData').slideDown("slow");

          $.ajaxSetup({
              scriptCharset: "utf-8",
              contentType: "application/x-www-form-urlencoded"
          });
          $.ajax({
              type: "POST",
              url: "dataCheck/processData",
              contentType: "application/x-www-form-urlencoded",
              data: {
                  headers: getColumnHeaders(),
                  firstLineIsData: $('#firstLineIsData').val(),
                  rawData: $('#copyPasteData').val()
              },
              success: function(data){
                //  $('#processedData').slideUp("slow");
                  $('#processedData').html(data);
                  $('#processedData').slideDown("slow");
              }
          });
      }

      var dataResourceUid = "";

      function updateStatus(uid){
      //  console.log('Uploading status for...' + uid);
        dataResourceUid = uid;
        $('#uploadProgressBar').show();
        $('#uploadFeedback').show();
        updateStatusPolling();
      }

      function randomString(length) {
          var chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz'.split('');

          if (! length) {
              length = Math.floor(Math.random() * chars.length);
          }

          var str = '';
          for (var i = 0; i < length; i++) {
              str += chars[Math.floor(Math.random() * chars.length)];
          }
          return str;
      }

      function updateStatusPolling(){

        $.get("dataCheck/uploadStatus?uid="+dataResourceUid+"&random=" + randomString(10), function(data){
          console.log("Retrieving status...." + data.status + ", percentage: " + data.percentage);
          if(data.status == "COMPLETE"){
            $("#uploadProgressBar").progressbar({ value: 100 });

            $('#spatialPortalLink').attr('href', 'dataCheck/redirectToSpatialPortal?uid=' + dataResourceUid);
            $('#hubLink').attr('href', 'dataCheck/redirectToBiocache?uid=' + dataResourceUid);
            $('#downloadLink').attr('href', 'dataCheck/redirectToDownload?uid=' + dataResourceUid);
            $('.ui-progressbar-value').html('<span>Dataset uploaded.</span>');
            $('#optionsAfterDownload').css({'display':'block'});

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
            { "rawData": $('#copyPasteData').val(),
              "headers": getColumnHeaders(),
              "datasetName": $('#datasetName').val(),
              "firstLineIsData": $('#firstLineIsData').val(),
              "customIndexedFields": $('#customIndexedFields').val()
            },
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
        var i = 0;
        $.each(columnHeaderInputs, function(index, input){
          if(index>0){
           columnHeadersCSV = columnHeadersCSV + ",";
          }
          columnHeadersCSV = columnHeadersCSV + input.value;
          i++;
        });
        console.log('Returning headers : ' + columnHeadersCSV);
        return columnHeadersCSV;
      }

      //setup the page
      $(document).ready(function(){

          $('#copyPasteData').keyup( function(){
              window.setTimeout('parseColumns()', 1500, true);
          });

          $('#copyPasteData').keydown( function(){
            //  reset();
          });

          init();

          //enable tabs in textareas
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
    </script>
  </head>
<body class="fluid">
<div id="content">
  <div id="wrapper" style="width:95%; padding:30px; text-align: left;">
      <h1>Sandbox</h1>
      <p style="width:80%;">
        This is a sandbox environment for data uploads, to allow users to view their data with ALA tools.
        This is currently an <strong>experimental</strong> feature of the Atlas.<br/> Uploaded data will be <strong>periodically cleared</strong> from the system.
        This tool accepts comma separated values (CSV) and tab separated data.
        <br/>
      </p>

      <div id="initialPaste">
        <h2>1. Paste your CSV data here</h2>
        <p>To paste your data, click the rectangle below, and type <strong>control-V (Windows)</strong>
            or <strong>command-V (Macintosh)</strong>. For a large amount of data, this may take a while to parse.
        </p>
        <g:textArea
            id="copyPasteData"
            name="copyPasteData" rows="15" cols="120"></g:textArea>
        <a href="javascript:parseColumns();" id="checkDataButton" class="button">Check Data</a>
        <p id="processingInfo"></p>
      </div><!-- initialPaste -->

      <div id="recognisedDataDiv">
        <h2>2. Check our initial interpretation</h2>

        <p>Adjust headings that have been incorrectly matched using the text boxes.
        Fields marked in <strong>yellow</strong> havent been matched to a recognised field name (<a href="http://rs.tdwg.org/dwc/terms/" target="_blank">darwin core terms</a>).<br/>

        After adjusting, click
        <a href="javascript:processedData();" id="processData2" name="processData2" class="button">Reprocess sample</a></p>
        <div id="recognisedData"></div>
        <a href="javascript:processedData();" id="processData" name="processData" class="button">Reprocess sample</a>
      </div><!-- recognisedDataDiv -->

      <div id="processSample">
        <h2>3. Process sample & upload to sandbox</h2>
        <div id="processSampleUpload">
          <p style="display:none;">
              <label for="customIndexedFields">Custom index fields</label>
              <input type="text" name="customIndexedFields" id="customIndexedFields" value=""/>
          </p>
          <p>
          The tables below display the first few records and our interpretation. The <strong>Processed value</strong>
          displays the results of name matching, sensitive data lookup and reverse geocoding where coordinates have been supplied.<br/>
          If you are happy with the initial processing, please give your dataset a name, and upload into the sandbox.
          This will process all the records and allow you to visualise your data on a map.
          </p>
          <p style="padding-bottom:0px;">
            <label for="datasetName" class="datasetName"><strong>Your dataset name</strong></label>
            <input id="datasetName" class="datasetName" name="datasetName" type="text" value="My test dataset" style="width:350px; margin-bottom:5px;"/>
            <a id="uploadButton" class="button" href="javascript:uploadToSandbox();">Upload your data</a>
            <div id="uploadFeedback" style="clear:right;">
            </div>
            <div id="uploadProgressBar">
            </div>
            <div id="optionsAfterDownload" style="display:none; margin-bottom: 0px; padding-bottom: 0px;">

                <h2 style="margin-top:25px; ">Options for working with your data</h2>

                <section class="meta">
                  <dl>
                    <dd><a href="#spatialPortalLink" id="spatialPortalLink" class="button" title="Mapping &amp; Analysis in the Spatial portal">Mapping & Analysis with your data</a></dd>
                    <dd><a href="#hubLink" id="hubLink" class="button" title="Tables &amp; charts for your data">Tables & charts of your data</a></dd>
                    <dd><a href="#downloadLink" id="downloadLink" class="button" title="Life Science Identifier (pop-up)">Download the processed version of your data</a></dd>
                  </dl>
                </section>
            </div>
          </p>
        </div>
      </div><!-- processedSample -->

      <div id="processedData"></div>
          <div id="jsonBlob" style="visibility: hidden; display:none;">
            <g:textArea id="columnHeaders" name="columnHeaders" cols="1000" rows="1000"><g:each in="${columnHeaders}"
               var="hdr">${hdr},</g:each></g:textArea>
            <g:textArea id="cachedData" name="cachedData" cols="1000" rows="1000"><g:each in="${dataRows}" var="row"><g:each
                    in="${row}" var="value">${value},</g:each>
            </g:each></g:textArea>
          </div>
      </div><!-- processedData -->
  </div><!-- wrapper -->
</div><!-- content -->
</body>
</html>