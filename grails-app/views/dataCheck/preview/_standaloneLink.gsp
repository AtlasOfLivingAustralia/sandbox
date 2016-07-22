<%@ page import="au.org.ala.web.CASRoles" %>
<g:link controller="tempDataResource" action="myData" class="btn btn-primary">My uploaded datasets</g:link>
<g:set var="authService" bean="authService"></g:set>
<g:if test="${authService.userInRole(au.org.ala.web.CASRoles.ROLE_ADMIN)}">
    <g:link controller="tempDataResource" action="adminList" class="btn btn-default">All Datasets</g:link>
</g:if>
