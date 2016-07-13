<%@ page contentType="text/html;charset=UTF-8" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>${grailsApplication.config.skin.appName} | ${grailsApplication.config.skin.orgNameLong}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="fluidLayout" content="true"/>
    <meta name="layout" content="${grailsApplication.config.skin.layout}-angular"/>
</head>

<body>
<ui-view>
    <div class="container">
        <div class="row">
            <div class="col-sm-12 text-center">
                <i class="fa fa-cog fa-2x fa-spin"><span class="sr-only">Loading...</span></i>
            </div>
        </div>
    </div>
</ui-view>

<g:render template="preview" />

<g:render template="allDatasets" />

<g:render template="datasets" />

<g:render template="chartOptions" />

<g:set var="loginLink"
       value="${grailsApplication.config.casServerLoginUrl}?service=${createLink(uri: '/dataCheck', absolute: true)}"/>

<g:render template="/alertModals" var="${loginLink}"/>

</body>
</html>