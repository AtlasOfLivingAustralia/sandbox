<%@ page contentType="text/html;charset=UTF-8" %>
<html xmlns="http://www.w3.org/1999/html">
  <head>
    <title>Sandbox | Atlas of Living Australia</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <r:require modules="progressbar"/>
    <r:script disposition='head'>

      if (!window.console) console = {log: function() {}};

      var SANDBOX = {
          currentPaste: "",
          dataResourceUid: ""
      }

      function init(){
        console.log("Initialising sandbox...");
        $('#recognisedDataDiv').hide();
        $('#processSample').hide();
        $('#processingInfo').hide();
        $('#uploadFeedback').html('');
        $('#progressBar').hide();
        $('#uploadFeedback').hide();
        if(typeof String.prototype.trim !== 'function') {
          String.prototype.trim = function() {
              return this.replace(/^\s+|\s+$/g, '');
          }
        }
      }

      function reset(){
         $('#processedContent').remove();
         $('#uploadFeedback').html('');
         $('#recognisedDataDiv').hide();
         $('#processSample').slideUp("slow");
         $('#progressBar').hide();
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
        if($('#copyPasteData').val().trim() == ""){
           reset();
        } else if(pasteAreaHasChanged()){

          $('#checkDataButton').addClass("disabled");
          $('#checkDataButton').html("Checking....");

          $('#processingInfo').html('<strong>Parsing the pasted input.....</strong>');
          $('#processedContent').html('');
          $('#uploadFeedback').html('');
          $('#uploadProgressBar').html('');
          //$('#uploadProgressBar').progressbar( "destroy" );
          $.ajaxSetup({
            scriptCharset: "utf-8",
            contentType: "text/html; charset=utf-8"
          });
          $.ajax({
             type: "POST",
             url: "dataCheck/parseColumns",
             data: $('#copyPasteData').val(),
             success: function(data){
               $('#checkDataButton').removeClass("disabled");
               console.log(data);
               $('#recognisedData').html(data)
               $('#recognisedDataDiv').slideDown("slow");
               processedData();
               $('#processSample').slideDown("slow");
               $('#processingInfo').html('<strong>&nbsp;</strong>');
               $('#firstLineIsData').change(function(){ parseColumnsWithFirstLineInfo(); });
               $('#checkDataButton').html("Check data");
             },
             error: function(){
                 $('#checkDataButton').removeClass("disabled");
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
          $('#processedData').slideUp("slow");
          $('.processDataBtn').addClass("disabled");
          $('.processDataBtn').text('Reprocessing.......');
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
                  $('#processedData').html(data);
                  $('#processedData').slideDown("slow");
                  $('.processDataBtn').text('Reprocess data');
                  $('.processDataBtn').removeClass("disabled");
              },
              error:function(){
                  $('.processDataBtn').text('Reprocess data');
                  $('.processDataBtn').removeClass("disabled");
              }
          });
      }

      function updateStatus(uid){
        SANDBOX.dataResourceUid = uid;
        $('#progressBar').show();

        $('.progress .bar').progressbar({
            use_percentage: true,
            display_text: 2,
            refresh_speed: 50,
            text:'Starting the upload...'
        });

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

        $.get("dataCheck/uploadStatus?uid="+SANDBOX.dataResourceUid+"&random=" + randomString(10), function(data){
          console.log("Retrieving status...." + data.status + ", percentage: " + data.percentage);
          if(data.status == "COMPLETE"){
             $('.progress .bar').attr('data-percentage', '100');
             $('#uploadFeedback').html('Upload complete.');
             $('.progress .bar').progressbar();
            $('#spatialPortalLink').attr('href', 'dataCheck/redirectToSpatialPortal?uid=' + SANDBOX.dataResourceUid);
            $('#hubLink').attr('href', 'dataCheck/redirectToBiocache?uid=' + SANDBOX.dataResourceUid);
            $('#downloadLink').attr('href', 'dataCheck/redirectToDownload?uid=' + SANDBOX.dataResourceUid);
            $('#optionsAfterDownload').css({'display':'block'});
            $("#uploadFeedback").html('');
          } else if(data.status == "FAILED"){
            $("#uploadFeedback").html('<span>Dataset upload <strong>failed</strong>. Please email support@ala.org.au with e details of your dataset.</span>');
          } else {
             $('.progress .bar').attr('data-percentage', data.percentage);
             $('.progress .bar').progressbar();
            $("#uploadFeedback").html('<span>Percentage completed: '+data.percentage+'%. </span><span>STATUS: '+data.status+', '+data.description+"</span>");
            setTimeout("updateStatusPolling()", 1000);
          }
        });
      }

      function uploadToSandbox(){
        console.log('Uploading to sandbox...');
        $('#uploadButton').removeClass("disabled");
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
    </r:script>
  </head>
<body>
<div class="container">
  <h1>Sandbox</h1>
  <div class="row-fluid" class="span12">
      <p>
        This is a sandbox environment for data uploads, to allow users to view their data with ALA tools.
        This is currently an <strong>experimental</strong> feature of the Atlas.<br/>
        Uploaded data will be <strong>periodically cleared</strong> from the system.
        This tool accepts comma separated values (CSV) and tab separated data.
        <br/>
        To upload file instead, <g:link controller="upload" action="index">click here</g:link>.
        <br/>
      </p>
      <div id="initialPaste">
        <h2>1. Paste your CSV data here</h2>

        <p>To paste your data, click the rectangle below, and type <strong>control-V (Windows)</strong>
            or <strong>command-V (Macintosh)</strong>. For a large amount of data, this may take a while to parse.
        </p>

        <g:textArea
            id="copyPasteData"
            class="span12"
            name="copyPasteData" rows="10" cols="120"></g:textArea>

        <a href="javascript:parseColumns();" id="checkDataButton" class="btn">Check data</a>
        <p id="processingInfo"></p>
      </div><!-- initialPaste -->

      <div id="recognisedDataDiv">
        <h2>2. Check our initial interpretation</h2>

        <p>Adjust headings that have been incorrectly matched using the text boxes.
        Fields marked in <strong>yellow</strong> havent been matched to a recognised field name
        (<a href="http://rs.tdwg.org/dwc/terms/" target="_blank">darwin core terms</a>).<br/>

        After adjusting, click
        <a href="javascript:processedData();" id="processData2" name="processData2" class="btn processDataBtn">Reprocess sample</a></p>
        <div class="well" >
            <div id="recognisedData"></div>
            <a href="javascript:processedData();" id="processData" name="processData" class="btn processDataBtn">Reprocess sample</a>
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
            <input id="datasetName" class="datasetName" name="datasetName" type="text" value="My test dataset" style="width:350px; margin-bottom:5px;"/>
            <a id="uploadButton" class="btn" href="javascript:uploadToSandbox();">Upload your data</a>

            <div id="uploadFeedback" style="clear:right;">
            </div>

            <div id="progressBar" class="progress progress-info" style="margin-top:20px;">
                <div class="bar" data-percentage="0"></div>
            </div>

            <div id="optionsAfterDownload" style="display:none; margin-bottom: 0px; padding-bottom: 0px;">

                <h2 style="margin-top:25px;">Options for working with your data</h2>

                <div class="row-fluid">
                  <a href="#spatialPortalLink" id="spatialPortalLink" class="btn" title="Mapping &amp; Analysis in the Spatial portal">Mapping & Analysis with your data</a></dd>
                  <a href="#hubLink" id="hubLink" class="btn" title="Tables &amp; charts for your data">Tables & charts of your data</a></dd>
                  <a href="#downloadLink" id="downloadLink" class="btn" title="Life Science Identifier (pop-up)">Download the processed version of your data</a></dd>
                </div>
            </div>
          </div>
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