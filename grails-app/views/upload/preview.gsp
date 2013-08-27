<%@ page contentType="text/html;charset=UTF-8" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>Sandbox file upload | Atlas of Living Australia</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <r:require modules="fileupload,progressbar"/>
    <r:script disposition='head'>

      if (!window.console) console = {log: function() {}};

      var SANDBOX = {
          dataResourceUid: "",
          fileId: "${params.id}"
      }

      function init(){
        console.log("Initialising sandbox...");
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

      function loadUploadedData(id){
          //$('#uploadProgressBar').progressbar( "destroy" );
          $.ajaxSetup({
            scriptCharset: "utf-8",
            contentType: "text/html; charset=utf-8"
          });
          $.ajax({
             type: "POST",
             url: "${createLink(controller:'upload', action:'parseColumns')}?id=" + id,
             success: function(data){
                 $('#checkDataButton').removeClass("disabled");
                 $('#recognisedData').html(data)
                 $('#firstLineIsData').change(function(){ parseColumnsWithFirstLineInfo(); });
             },
             error: function(){
                 $('#checkDataButton').removeClass("disabled");
                 $('#checkDataButton').html("Check data");
             }
         });
        }

      function parseColumnsWithFirstLineInfo(){
          console.log("Parsing first line to do interpretation...");
          $.post("${createLink(controller:'upload', action:'parseColumnsWithFirstLineInfo')}?id=${params.id}&firstLineIsData="+$('#firstLineIsData').val(),
          {},
             function(data){
               $('#recognisedData').html(data)
               $('#recognisedDataDiv').slideDown("slow");
               processedData('${params.id}');
               $('#processSample').slideDown("slow");
               $('#processingInfo').html('<strong>&nbsp;</strong>');
               $('#firstLineIsData').change(function(){parseColumnsWithFirstLineInfo();});
             }, "html"
          );
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

        $.get("${createLink(controller:'dataCheck', action:'uploadStatus')}?uid="+SANDBOX.dataResourceUid+"&random=" + randomString(10), function(data){
          console.log("Retrieving status...." + data.status + ", percentage: " + data.percentage);
          if(data.status == "COMPLETE"){
            $('.progress .bar').attr('data-percentage', '100');
            $('#uploadFeedback').html('Upload complete.');
            $('.progress .bar').progressbar();
            $('#spatialPortalLink').attr('href', '${createLink(controller:'dataCheck', action:'redirectToSpatialPortal')}?uid=' + SANDBOX.dataResourceUid);
            $('#hubLink').attr('href', '${createLink(controller:'dataCheck', action:'redirectToBiocache')}?uid=' + SANDBOX.dataResourceUid);
            $('#downloadLink').attr('href', '${createLink(controller:'dataCheck', action:'redirectToDownload')}?uid=' + SANDBOX.dataResourceUid);
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
        $.ajax({
            type:"POST",
            url: "${createLink(controller:'upload', action:'uploadToSandbox')}",
            data: JSON.stringify({
              "fileId" : SANDBOX.fileId,
              "headers": getColumnHeaders(),
              "datasetName": $('#datasetName').val(),
              "firstLineIsData": $('#firstLineIsData').val()
            }),
            success: function(data){
                updateStatus(data.uid);
            },
            error: function(){
                alert("There was a problem starting the upload. Please email support@ala.org.au");
            },
            dataType: 'json',
            contentType: "application/json",
        });
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
              url: "${createLink(controller:'upload', action:'viewProcessData')}?id=" + SANDBOX.fileId,
              contentType: "application/x-www-form-urlencoded",
              data: {
                  firstLineIsData: $('#firstLineIsData').val()
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

      //setup the page
      $(document).ready(function(){
           loadUploadedData('${params.id}');
           init();
      });

    </r:script>
</head>

<body>
    <div class="container">

      <h1>Sandbox file upload <i>(Early Alpha)</i></h1>

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
              <div class="bs-callout bs-callout-info">
              The tables below display the first few records and our interpretation. The <strong>Processed value</strong>
              displays the results of name matching, sensitive data lookup and reverse geocoding where coordinates have been supplied.<br/>
              If you are happy with the initial processing, please give your dataset a name, and upload into the sandbox.
              This will process all the records and allow you to visualise your data on a map.
              </div>

              <div class="well">
                <label for="datasetName" class="datasetName"><strong>Your dataset name</strong></label>

                <input id="datasetName" class="datasetName" name="datasetName" type="text" value="${params.fn}" style="width:350px; margin-bottom:5px;"/>

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
      </div>
    </div>
</body>
</html>