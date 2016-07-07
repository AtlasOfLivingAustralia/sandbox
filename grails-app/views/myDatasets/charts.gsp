<%@ page contentType="text/html;charset=UTF-8" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>Chart options | ${grailsApplication.config.skin.appName} | ${grailsApplication.config.skin.orgNameLong}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="${grailsApplication.config.skin.layout}" />
    <link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
    <script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
</head>
<body>
<div class="panel panel-default">
    <div class="panel-heading">
        <h1>${metadata.name} - Chart display options</h1>
    </div>
    <div class="panel-body">
    <g:form action="saveChartOptions">
        <input type="hidden" name="tempUid" value="${tempUid}"/>

    <div class="pull-right">
        <input class="btn btn-primary" type="submit" value="Save" />
    </div>
    <p>
        Configure the chart to show for your dataset.<br/>
        You can drag and drop the table to adjust the order the charts are presented.
        <br/>
        Please click save to store your configuration for this dataset.
    </p>
    <table class="table table-condensed table-striped">
        <thead>
        <tr>
            <th>Field name</th>
            <th>Chart type</th>
            <th>Show</th>
        </tr>
        </thead>
        <tbody class="customIndexes">
        <g:each in="${chartConfig}" var="chartCfg" status="indexStatus">
            <tr>
                <td>
                    <g:formatFieldName value="${chartCfg.field}"/>
                    <input type="hidden" name="field" value="${chartCfg.field}"/>
                </td>
                <td>
                    <g:select name="format" from="['pie', 'bar', 'column', 'line', 'scatter']" value="${chartCfg.format}"/>
                </td>
                <td>
                    <input type="hidden" name="visible_${indexStatus}" value="off" />
                    <input type="checkbox" name="visible_${indexStatus}"
                           <g:if test="${chartCfg.visible.toBoolean()}">checked="true"</g:if>
                    />
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
    <div class="pull-right">
        <input class="btn btn-primary" type="submit" value="Save" />
        <g:link class="btn btn-default" controller="myDatasets">Cancel</g:link>
    </div>

    </g:form>
    </div>
</div>
</body>

<r:script>
    $(function  () {
        $(".customIndexes").sortable();
        $( ".customIndexes" ).disableSelection();
    });
</r:script>

</html>