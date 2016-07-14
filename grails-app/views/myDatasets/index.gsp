<%@ page contentType="text/html;charset=UTF-8" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>My datasets | ${grailsApplication.config.skin.appName} | ${grailsApplication.config.skin.orgNameLong}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
</head>

<body>
<div class="well pull-right col-sm-3">
    <strong>Tip:</strong> To share your list of datasets with other users, use
<g:link controller="myDatasets" action="userDatasets" params="${[userId: currentUserId]}">this link</g:link>.
</div>
<h1>${grailsApplication.config.skin.appName} - My uploaded datasets</h1>
<div class="panel panel-default">
    <div class="panel-body">
        <div class="row">
            <div class="col-sm-12">
                <g:if test="${params.containsKey("deleteSuccess")}">
                    <g:if test="${params.deleteSuccess == 'false'}">
                        <div class="alert alert-danger" role="alert">
                            Unable to delete this dataset.
                        </div>

                    </g:if>
                    <g:else>
                        <div class="alert alert-info" role="alert">
                            Dataset deleted.
                        </div>
                    </g:else>
                </g:if>
                <p>
                    Here is a listing of your previously uploaded datasets.<br/>
                </p>
                <g:link controller="dataCheck" action="index" class="btn btn-primary">Create new dataset</g:link>
            </div>
        </div>
    </div>
    <table class="table">
        <thead>
        <tr>
            <th>Dataset name</th>
            <th>Date uploaded</th>
            <th>Number of records</th>
            <th></th>
        </tr>
        </thead>
        <g:each in="${userUploads}" var="userUpload">
            <tr>
                <td>${userUpload.name}</td>
                <td><prettytime:display
                        date="${new Date().parse("yyyy-MM-dd'T'HH:mm:ss", userUpload.dateCreated)}"/></td>
                <td>${userUpload.numberOfRecords}</td>
                <td>
                    <a class="btn btn-default"
                       href="${userUpload.uiUrl ?: grailsApplication.config.sandboxHubsWebapp}/occurrences/search?q=data_resource_uid:${userUpload.uid}"><i
                            class="fa fa-th-list"></i> View records</a>

                    <g:link class="btn btn-default" controller="myDatasets" action="chartOptions"
                            params="${[tempUid: userUpload.uid]}">
                        <i class="fa fa-cog"></i> Configure charts
                    </g:link>

                    <g:link class="btn btn-default" controller="dataCheck" action="reload"
                            params="${[dataResourceUid: userUpload.uid]}">
                        <i class="fa fa-repeat"></i> Reload
                    </g:link>

                    <g:form style="display:inline-block" action="deleteResource" method="POST"><g:hiddenField name="uid" value="${userUpload.uid}"/><button type="submit" class="btn btn-danger"><i class="fa fa-trash"></i> Delete</button></g:form>
                </td>
            </tr>
        </g:each>
    </table>
</div>
</body>
</html>