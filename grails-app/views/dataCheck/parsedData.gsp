<%@ page contentType="text/html;charset=UTF-8" %>

<div id="interpretation">
  <label for="firstLineIsData">The first line is: </label>
  <select id="firstLineIsData" name="firstLineIsData">
    <option value="true"  <g:if test="${firstLineIsData}">selected="true"</g:if>>Data</option>
    <option value="false" <g:if test="${!firstLineIsData}">selected="true"</g:if>>Column headers</option>
  </select>
</div>

<div id="tabulatedData">
    <style type="text/css">
      table { border-collapse: collapse; }
      th { font-size: 12px; border-collapse: collapse; border: 1px solid #000000; padding:2px; background-color: #000000; color: #ffffff;}
      td { font-size: 11px; border-collapse: collapse; border: 1px solid #000000; padding: 2px;}
    </style>
  <h1> </h1>
<table id="initialParse">
    <thead>
    <g:if test="${columnHeaderMap}">
      <g:each in="${columnHeaderMap}" var="hdr">
        <th>
          <input class="columnHeaderInput" type="text" value="${hdr.value ? hdr.value : hdr.key}" style="${hdr.value != '' ? '' : 'background-color: #E9AB17;'}"/>
        </th>
      </g:each>
    </g:if>
    <g:else>

     <g:set var="unknownCounter" value="0" />
      <g:each in="${columnHeaders}" var="header">
        <th>
           <g:if test="${!header}">
            <g:set var="unknownCounter" value="${unknownCounter.toInteger() + 1}" />
           </g:if>
          <input class="columnHeaderInput" type="text" value="${header ? header : 'Unknown ' + unknownCounter}" style="${header ? '' : 'background-color: #E9AB17;'}"/>
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
  <div id="jsonBlob" style="visibility: hidden; display:none;">
    <g:textArea id="columnHeaders" name="columnHeaders" cols="1000" rows="1000"><g:each in="${columnHeaders}" var="hdr">${hdr},</g:each></g:textArea>
  </div>
</div>
