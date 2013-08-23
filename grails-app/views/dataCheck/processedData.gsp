<%@ page contentType="text/html;charset=UTF-8" %>
<div id="processedContent">
  <style>
    .fieldNameTD { background-color: #f9f9f9 }
    #processedSampleTable th  { background-color: #f9f9f9 }
    .assertionName { background-color: #f9f9f9 }
    .recordHeader { border: 1px solid black; border-collapse: collapse; background-color: black; color:white; font-weight: bold;}
    .assertionHeader {background-color: black; color:white; text-align: left; ; padding-left:10px;}
    .assertionName { text-align: right; ; padding-right:5px;}
    .sensitiveField {background-color: #f2dede; }
    .changedValue { color:#006400; }
    .dataQualityHdr { padding-right:20px; }
    .fieldNameTD { text-align: right; padding-right:5px;}
    #processedSampleTable td { max-width:410px; }
    #processedSampleTable th { min-width:300px; }
  </style>
    <g:each in="${processedRecords}" var="processedRecord" status="recordStatus">
      <table id="processedSampleTable" class="table table-bordered" >
        <thead>
          <th class="fieldNameTD">Field name</th><th>Original value</th><th>Processed value</th>
        </thead>
        <g:each in="${processedRecord.values}" var="field">
        <tr>
          <td class="${field.name == "informationWithheld" || field.name == "dataGeneralizations" ? "sensitiveField" : "fieldName" } fieldNameTD">
            <g:prettyCamel value="${field.name}"/>
          </td>
          <td>${field.raw}</td>
          <td class="${field.processed != field.raw && field.processed != null ? 'changedValue' : 'originalConfirmed'}">
              <dc:formatProperty value="${field.processed}"/>
          </td>
        </tr>
        </g:each>
        <g:if test="${processedRecord.assertions}">
          <tr class="error">
            <td colspan="3" class="error XXassertionHeader" colspan="1">
                <span class="dataQualityHdr">Data quality tests for this record</span>
                <span class="label label-success">Passed <dc:countByQaStatus assertions="${processedRecord.assertions}" qaStatus="1" /></span>
                <span class="label label-important"> Warnings <dc:countByQaStatus assertions="${processedRecord.assertions}" qaStatus="0" /></span>
            </td>
          </tr>
        </g:if>
        <g:each in="${processedRecord.assertions}" var="assertion">
            <g:set var="cssClass"><g:if test="${assertion.qaStatus == "0"}">important</g:if><g:elseif test="${assertion.qaStatus == "1"}">success</g:elseif><g:elseif test="${assertion.qaStatus == "2"}">warning</g:elseif></g:set>
            <g:set var="result"><g:if test="${assertion.qaStatus == "0"}">warning</g:if>
                <g:elseif test="${assertion.qaStatus == "1"}">passed</g:elseif>
                <g:elseif test="${assertion.qaStatus == "2"}">not checked</g:elseif></g:set>
            <g:set var="qaComment">
                <g:if test="${assertion.qaStatus == "0"}">Failed quality test.</g:if>
                <g:elseif test="${assertion.qaStatus == "1"}">Passed quality test.</g:elseif>
                <g:elseif test="${assertion.qaStatus == "2"}">This test was not ran due to lack of data.</g:elseif>
            </g:set>
            <tr>
              <td class="assertionName">
                  <g:message code="${assertion.name}" default="${assertion.name}"/>
              </td>
              <td colspan="2"><span class="label label-${cssClass}">${result}</span></td>
            </tr>
        </g:each>
      </table>
    </g:each>
</div>