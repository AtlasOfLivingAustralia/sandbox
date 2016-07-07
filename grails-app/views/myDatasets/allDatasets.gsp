<%@ page contentType="text/html;charset=UTF-8" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>My datasets | ${grailsApplication.config.skin.appName} | ${grailsApplication.config.skin.orgNameLong}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
</head>

<body>
<div class="panel panel-default">
    <div class="panel-heading">
        <h1>${grailsApplication.config.skin.appName} -  All datasets</h1>
    </div>

    <div class="panel-body">
        <g:if test="${userUploads}">

            <g:if test="${params.containsKey("deleteSuccess")}">
                <g:if test="${params.deleteSuccess}">
                    <div class="alert alert-info" role="alert">
                        Dataset deleted.
                    </div>
                </g:if>
                <g:else>
                    <div class="alert alert-error" role="alert">
                        Unable to delete this dataset.
                    </div>
                </g:else>
            </g:if>

            <p class="lead">
                Here is a listing of uploaded datasets in this system.
            </p>

        </g:if>
        <g:else>
            <p class="lead">
                You currently have no uploaded datasets. <g:link uri="/">Click here</g:link> to upload data.
            </p>
        </g:else>
    </div>
    <g:if test="${userUploads}">
        <table class="table">
            <thead>
            <tr>
                <th>Dataset name</th>
                <th>Date uploaded</th>
                <th>Uploaded by</th>
                <th>Number of records</th>
                <th></th>
            </tr>
            </thead>
            <g:each in="${userUploads}" var="userUpload">
                <g:if test="${userUpload.webserviceUrl && userUpload.webserviceUrl.startsWith(grailsApplication.config.biocacheServiceUrl)}">
                    <tr>
                        <td>${userUpload.name}</td>
                        <td><prettytime:display
                                date="${new Date().parse("yyyy-MM-dd'T'HH:mm:ss", userUpload.dateCreated)}"/></td>
                        <td>${userUpload.userDisplayName}</td>
                        <td>${userUpload.numberOfRecords}</td>
                        <td>
                            <a class="btn"
                               href="${userUpload.uiUrl ?: grailsApplication.config.sandboxHubsWebapp}/occurrences/search?q=data_resource_uid:${userUpload.uid}"><i
                                    class="icon-th-list"></i> View records</a>
                        </td>
                    </tr>
                </g:if>
            </g:each>
        </table>
    </g:if>
</div>
</body>
</html>