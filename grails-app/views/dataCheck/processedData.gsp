<%@ page contentType="text/html;charset=UTF-8" %>
<div id="processedContent">
  <style>
    .fieldName { border: 1px solid black; border-collapse: collapse; background-color: gray; color:white; }
    .recordHeader { border: 1px solid black; border-collapse: collapse; background-color: black; color:white; font-weight: bold;}
    .assertionHeader {background-color: black; color:white; text-align: left; ; padding-left:10px;}
    .assertionName {background-color: red; color:white; text-align: right; ; padding-right:5px;}
    .sensitiveField {background-color: red; color:white;}
    .changedValue { color:#006400; }
    .fieldNameTD { text-align: right; padding-right:5px;}
    #processedSampleTable td { max-width:410px; }
  </style>
    <g:each in="${processedRecords}" var="processedRecord" status="recordStatus">
      <table id="processedSampleTable">
        <thead>
          <th class="fieldNameTD">Field name</th><th>Original value</th><th>Processed value</th>
        </thead>
        <g:each in="${processedRecord.values}" var="field">
        <tr>
          <td class="${field.name == "informationWithheld" || field.name == "dataGeneralizations" ? "sensitiveField" : "fieldName" } fieldNameTD">
            <g:prettyCamel value="${field.name}"/>
          </td>
          <td>${field.raw}</td>
          <td class="${field.processed != field.raw && field.processed != null ? 'changedValue' : 'originalConfirmed'}">${field.processed}</td>
        </tr>
        </g:each>
        <g:if test="${processedRecord.assertions}">
          <tr>
            <td class="assertionHeader" colspan="3">Quality assertions</td>
          </tr>
        </g:if>
        <g:each in="${processedRecord.assertions}" var="assertion">
        <tr>
          <td class="assertionName"><g:message code="${assertion.name}" default="${assertion.name}"/></td>
          <td colspan="2">${assertion.comment}</td>
        </tr>
        </g:each>
      </table>
    </g:each>
</div>