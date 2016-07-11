<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en-AU">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="app.version" content="${g.meta(name:'app.version')}"/>
    <meta name="app.build" content="${g.meta(name:'app.build')}"/>
    <meta name="description" content="Atlas of Living Australia"/>
    <meta name="author" content="Atlas of Living Australia">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="http://www.ala.org.au/wp-content/themes/ala2011/images/favicon.ico" rel="shortcut icon"  type="image/x-icon"/>
    <base href="${g.createLink(uri: '/')}"/>

    <title><g:layoutTitle /></title>

    <r:require modules="application"/>

    <r:layoutResources/>
    <g:layoutHead />

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
	  <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
	  <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
  <![endif]-->
</head>
<body class="${pageProperty(name:'body.class')}" id="${pageProperty(name:'body.id')}" onload="${pageProperty(name:'body.onload')}" ng-app="ala.sandbox">

<!-- Header -->
<hf:banner logoutUrl="${g.createLink(controller:"logout", action:"logout", absolute: true)}" />
<!-- End header -->
<g:set var="fluidLayout" value="${pageProperty(name:'meta.fluidLayout')?:grailsApplication.config.skin?.fluidLayout}"/>
<!-- Container -->
<div class="${fluidLayout ? 'container-fluid' : 'container'}" id="main">
    <g:layoutBody />
</div><!-- End container #main col -->

<!-- Footer -->
<hf:footer/>
<!-- End footer -->

<!-- JS resources-->
<r:script>
  sandbox({
      loginUrl: '${grailsApplication.config.casServerLoginUrl}?service=${createLink(uri: '/', absolute: true)}',
      parseColumnsUrl: '${createLink(controller:'dataCheck', action:'parseColumns')}',
      processDataUrl: '${createLink(controller:'dataCheck', action:'processData')}',
      uploadCsvUrl: '${createLink(controller:'dataCheck', action:'uploadFile')}',
      uploadToSandboxUrl: '${createLink(controller:'dataCheck', action:'upload')}',
      uploadStatusUrl: '${createLink(controller:'dataCheck', action:'uploadStatus')}',
      userId: '${u.userId()}'
  });
</r:script>
<r:layoutResources disposition="defer"/>
</body>
</html>