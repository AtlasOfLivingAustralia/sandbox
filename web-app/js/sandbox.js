var SANDBOX = {
    dataResourceUid: "",
    currentPaste:""
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
            updateStatus(data.uid);
        }
    ).fail(function(jqXHR) {
        if(jqXHR.status == 401){
            $('#loginAlertModel').modal({})
        } else {
            alert('Fail:' + jqXHR.status);
        }
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