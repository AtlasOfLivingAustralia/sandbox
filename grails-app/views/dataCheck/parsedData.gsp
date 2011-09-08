<%@ page contentType="text/html;charset=UTF-8" %>
<div>
    <style type="text/css">
      table { border-collapse: collapse; }
      th { font-size: 12px; border-collapse: collapse; border: 1px solid #000000; padding:2px; background-color: #000000; color: #ffffff;}
      td { font-size: 11px; border-collapse: collapse; border: 1px solid #000000; padding: 2px;}
    </style>
  <h1> </h1>
<table>
    <thead>
    <g:if test="${columnHeaderMap}">
      <g:each in="${columnHeaderMap}" var="hdr">
        <th>
          <input class="columnHeaderInput" type="text" value="${hdr.value ? hdr.value : hdr.key}" style="${hdr.value != '' ? '' : 'background-color: #E9AB17;'}"/>
        </th>
      </g:each>
    </g:if>
    <g:else>
      <g:each in="${columnHeaders}" var="header">
        <th>
          <input class="columnHeaderInput" type="text" value="${header ? header : 'Unknown'}" style="${header ? '' : 'background-color: #E9AB17;'}"/>
        </th>
      </g:each>
    </g:else>
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