<%@ page contentType="text/html;charset=UTF-8" %>
<div>
    <style type="text/css">
      table { border-collapse: collapse; }
      th { font-size: 12px; border-collapse: collapse; border: 1px; padding:2px; background-color: #000000; color: #ffffff;}
      td { font-size: 11px; border-collapse: collapse; border: 1px;}
    </style>
<table>
    <thead>
    <g:each in="${columnHeaders}" var="header">
      <th>
        <g:if test="${header}">${header}</g:if>
        <g:else><span class="unrecognised">Unknown</span></g:else>
       </th>
    </g:each>
    </thead>
    <tbody>
    <g:each in="${dataRows}" var="row">
      <tr>
      <g:each in="${row}" var="value">
        <td>${value}</td>
      </g:each>
      </tr>
    </g:each>
    </tbody>
  </table>
  <input id="firstLineIsData" name="firstLineIsData" type="hidden" value="${firstLineIsData}"/>
  <div id="jsonBlob" style="visibility: hidden; display:none;">
    <g:textArea id="columnHeaders" name="columnHeaders" cols="1000" rows="1000"><g:each in="${columnHeaders}" var="hdr">${hdr},</g:each></g:textArea>
  </div>
</div>