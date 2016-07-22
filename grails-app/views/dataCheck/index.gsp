<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>${grailsApplication.config.skin.appName} | ${grailsApplication.config.skin.orgNameLong}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="fluidLayout" content="true" />
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <g:render template="/configjs" />
    <r:require module="preview" />
    <r:script>
      angular.module('ala.sandbox.preview').value('existing', <dc:json value="${dataResource}"/> );
    </r:script>
</head>
<body>
<div class="col-sm-12 col-md-12" ng-app="ala.sandbox" ng-controller="PreviewCtrl as preview" ng-cloak>
    <g:render template="/dataCheck/preview/shared" />
</div>
</body>
</html>