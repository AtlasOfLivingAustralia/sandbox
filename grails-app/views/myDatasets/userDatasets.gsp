<%@ page contentType="text/html;charset=UTF-8" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>${user?.displayName} datasets | ${grailsApplication.config.skin.appName} | ${grailsApplication.config.skin.orgNameLong}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="${grailsApplication.config.skin.layout}" />
</head>
<body>
<h1>${user?.displayName}'s datasets</h1>
<g:if test="${userUploads}">

    <p class="lead">
        Here is a listing of uploaded datasets in this system uploaded by ${user?.displayName}.
    </p>
    <div class="row-fluid" class="span12">
        <table class="table">
            <thead>
            <th>Dataset name</th>
            <th>Date uploaded</th>
            <th>Number of records</th>
            <th> </th>
            </thead>
            <g:each in="${userUploads}" var="userUpload">
                <tr>
                    <td>${userUpload.name}</td>
                    <td><prettytime:display date="${new Date().parse("yyyy-MM-dd'T'HH:mm:ss", userUpload.dateCreated)}" /></td>
                    <td>${userUpload.numberOfRecords}</td>
                    <td>
                        <a class="btn" href="${userUpload.uiUrl ?: grailsApplication.config.sandboxHubsWebapp}/occurrences/search?q=data_resource_uid:${userUpload.uid}"><i class="icon-th-list"></i> View records</a>
                    </td>
                </tr>
            </g:each>
        </table>
    </div>
</g:if>
<g:else>
    <p class="lead">
        This user has not yet uploaded a dataset to this system.
</g:else>
</div>
</body>
</html>