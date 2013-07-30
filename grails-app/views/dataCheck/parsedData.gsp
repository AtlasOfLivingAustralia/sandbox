<%@ page contentType="text/html;charset=UTF-8" %>

<script type="text/javascript">
    //var dwcURL = 'http://localhost:8080/ala-datacheck/dataCheck/autocomplete.json'
    jQuery("input.columnHeaderInput").autocomplete('dataCheck/autocomplete.json', {
        extraParams: {limit: 100},
        dataType: 'json',
        matchSubset: false,
        cacheLength: 10,
        minChars: 3,
        scroll: false,
        max: 10,
        selectFirst: false,
        parse: function(data){
            var rows = new Array();
            for(var i=0; i<data.length; i++){
                rows[i] = {
                    data: data[i],
                    value: data[i],
                    result:data[i]
                }
            }
            return rows;
        },
        formatItem: function(row, i, n){
            return row;
        }
    });
</script>

<div id="interpretation">
  <label for="firstLineIsData">The first line is: </label>
  <select id="firstLineIsData" name="firstLineIsData">
    <option value="true"  <g:if test="${firstLineIsData}">selected="true"</g:if>>Data</option>
    <option value="false" <g:if test="${!firstLineIsData}">selected="true"</g:if>>Column headers</option>
  </select>
</div>

<div id="tabulatedData"  style="overflow: auto;">
    <style type="text/css">
        #initialParse th input {margin-bottom:0px;}
      %{--table { border-collapse: collapse; }--}%
      %{--th { font-size: 12px; border-collapse: collapse; border: 1px solid #000000; padding:2px; background-color: #000000; color: #ffffff;}--}%
      %{--td { font-size: 11px; border-collapse: collapse; border: 1px solid #000000; padding: 2px;}--}%
    </style>
  <h1> </h1>
<table id="initialParse" class="table table-bordered" >
    <thead>

    <g:if test="${columnHeaderMap}">
      <g:each in="${columnHeaderMap}" var="hdr">
        <th>
          <input class="columnHeaderInput"
                 type="text"
                 autocomplete="off" name="q"
                 value="${hdr.value ? hdr.value : hdr.key}"
                 style="${hdr.value != '' ? '' : 'background-color: #fcf8e3;'}"
          />
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
          <input class="columnHeaderInput" type="text" autocomplete="off" name="q"
                 value="${header ? header : 'Unknown ' + unknownCounter}"
                 style="${header ? '' : 'background-color: #fcf8e3;'}"/>
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
