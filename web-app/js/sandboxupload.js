var SANDBOX = {
    dataResourceUid: ""
}

var SANDBOXUPLOAD = {
    fileId:  "",
    uploadStatusUrl: "",
    spatialPortalLink: "",
    hubLink: "",
    downloadLink: "",
    uploadLink: "",
    parseColumnsWithFirstLineInfoUrl: "",
    viewProcessDataUrl: "",
    parseColumnsUrl:""
};

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
    console.log('Load uploaded data with id ' + id)
    $.ajaxSetup({
        scriptCharset: "utf-8",
        contentType: "text/html; charset=utf-8"
    });
    $.ajax({
        type: "POST",
        url: SANDBOXUPLOAD.parseColumnsUrl + id,
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
    $.post(SANDBOXUPLOAD.parseColumnsWithFirstLineInfoUrl + $('#firstLineIsData').val(),
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

    $.get(SANDBOXUPLOAD.uploadStatusUrl + SANDBOX.dataResourceUid + "&random=" + randomString(10), function(data){
        console.log("Retrieving status...." + data.status + ", percentage: " + data.percentage);
        if(data.status == "COMPLETE"){
            $('.progress .bar').attr('data-percentage', '100');
            $('#uploadFeedback').html('Upload complete.');
            $('.progress .bar').progressbar();
            $('#spatialPortalLink').attr('href', SANDBOXUPLOAD.spatialPortalLink + SANDBOX.dataResourceUid);
            $('#hubLink').attr('href', SANDBOXUPLOAD.hubLink + SANDBOX.dataResourceUid);
            $('#downloadLink').attr('href', SANDBOXUPLOAD.downloadLink + SANDBOX.dataResourceUid);
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

    var uploadInfo = {
        "fileId" : SANDBOXUPLOAD.fileId,
        "headers": getColumnHeaders(),
        "datasetName": $('#datasetName').val(),
        "firstLineIsData": $('#firstLineIsData').val(),
        "dataResourceUid": $('#dataResourceUid').val()
    };

    console.log(uploadInfo);

    $.ajax({
        type:"POST",
        url: SANDBOXUPLOAD.uploadLink,
        data: JSON.stringify(uploadInfo),
        success: function(data){
            if(data && data.hasOwnProperty('uid')){
                updateStatus(data.uid);
            } else {
                alert("There was a problem starting the upload. Please email support@ala.org.au");
            }
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
        url: SANDBOXUPLOAD.viewProcessDataUrl + SANDBOXUPLOAD.fileId,
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