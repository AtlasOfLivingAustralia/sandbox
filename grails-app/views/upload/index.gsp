<%@ page contentType="text/html;charset=UTF-8" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>${grailsApplication.config.skin.appName} | File upload |  ${grailsApplication.config.skin.orgNameLong}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <r:require modules="jasnybootstrap"/>
</head>

<body>
<div class="panel panel-default">
    <div class="panel-heading">
        <g:if test="${reload}">
            <h1>${grailsApplication.config.skin.appName} - Reload dataset : ${dataResource.name}</h1>
        </g:if>
        <g:else>
            <h1>${grailsApplication.config.skin.appName} file upload</h1>
        </g:else>
    </div>

    <div class="panel-body">

        <div class="row">
            <div class="col-sm-12">
                <p>
                    This is a sandbox environment for data uploads, to allow users to view their data with ALA tools.
                    This is currently an <strong>experimental</strong> feature of the Atlas.<br/>
                    Uploaded data will be <strong>periodically cleared</strong> from the system.
                    <br/>
                    This tool accepts Excel spreadsheets and
                    <a target="_blank" href="http://en.wikipedia.org/wiki/Comma-separated_values">CSV</a> files.
                For large files, its recommended that the files are
                    <a target="_blank" href="http://en.wikipedia.org/wiki/Zip_(file_format)">zipped</a>
                    before uploading.
                </p>
                <g:uploadForm class="form" action="uploadFile">
                    <div class="form-group">
                        <div class="fileinput fileinput-new input-group col-sm-3" data-provides="fileinput">
                            <div class="form-control" data-trigger="fileinput"><i class="fa fa-file fileinput-exists"></i> <span class="fileinput-filename"></span></div>
                            <span class="input-group-addon btn btn-default btn-file"><span class="fileinput-new">Select file</span><span class="fileinput-exists">Change</span><input type="file" name="..."></span>
                            <a href="#" class="input-group-addon btn btn-default fileinput-exists" data-dismiss="fileinput">Remove</a>
                        </div>
                    </div>

                    <g:if test="${reload}">
                        <div id="hiddenBits">
                            <input type="hidden" name="dataResourceUid" value="${dataResource.uid}"/>
                            <input type="hidden" name="datasetName" value="${dataResource.name}"/>
                        </div>
                    </g:if>

                    <div style="clear:both">
                        <input type="submit" class="btn fileupload-exists btn-primary" value="Upload"/>
                        <span class="btn btn-default cancel">Cancel</span>
                    </div>

                </g:uploadForm>
            </div>
        </div>
    </div>
</div>
</body>
</html>