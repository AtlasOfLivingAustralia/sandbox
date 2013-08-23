<%@ page contentType="text/html;charset=UTF-8" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>Sandbox file upload | Atlas of Living Australia</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <r:require modules="fileupload"/>
</head>
<body>
    <div class="container">
      <h1>Sandbox file upload <span style="font-family: 'Courier New'">(Early Alpha)</span></h1>
      <div class="row-fluid" class="span12">
        <p>
            This is a sandbox environment for data uploads, to allow users to view their data with ALA tools.
            This is currently an <strong>experimental</strong> feature of the Atlas.<br/>
            Uploaded data will be <strong>periodically cleared</strong> from the system.
            <br/>
            This tool accepts Excel spreadsheets and <a target="_blank" href="http://en.wikipedia.org/wiki/Comma-separated_values">CSV</a> files.
            For large files, its recommended that the files are <a target="_blank" href="http://en.wikipedia.org/wiki/Zip_(file_format)">zipped</a> before uploading.
        </p>
        <g:uploadForm action="uploadFile">
            <div class="fileupload fileupload-new span9" data-provides="fileupload">
              <div class="input-append">
                <div class="uneditable-input span3">
                  <i class="icon-file fileupload-exists"></i>
                  <span class="fileupload-preview"></span>
                </div>
                <span class="btn btn-file">
                  <span class="fileupload-new">Select file</span>
                  <span class="fileupload-exists">Change</span>
                  <input type="file" name="myFile" />
                </span>
                <a href="#" class="btn fileupload-exists" data-dismiss="fileupload">Remove</a>
              </div>
                <div style="clear:both">
                  <input type="submit" class="btn fileupload-exists btn-primary" value="Upload"/>
                  <span class="btn cancel">Cancel</span>
                </div>
            </div>
        </g:uploadForm>
      </div>
    </div>
</body>
</html>