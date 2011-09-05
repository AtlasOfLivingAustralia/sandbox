<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head><title>Simple GSP page</title></head>
  <style>
    .fieldName { border: 1px solid black; border-collapse: collapse; background-color: gray; color:white; }
    .recordHeader { border: 1px solid black; border-collapse: collapse; background-color: black; color:white; font-weight: bold;}
    .assertionName {background-color: red; color:white;}
  </style>
  <body>
    <g:each in="${processedRecords}" var="processedRecord" status="recordStatus">
      <table>
        <thead>
        <th colspan="3" class="recordHeader">Record ${recordStatus.intValue() + 1}</th>
        </thead>
        <g:each in="${processedRecord.values}" var="field">
        <tr>
          <td class="fieldName">${field.name}</td>
          <td>${field.raw}</td>
          <td>${field.processed}</td>
        </tr>
        </g:each>
        <g:each in="${processedRecord.assertions}" var="assertion">
        <tr>
          <td class="assertionName">${assertion.name}</td>
          <td colspan="2">${assertion.comment}</td>
        </tr>
        </g:each>
      </table>
    </g:each>
  </body>
</html>