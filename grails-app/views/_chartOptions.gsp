<script type="text/ng-template" id="chartOptions.html">
<div class="container" id="main" ng-cloak>
    <div class="row">
        <div class="col-sm-12">
            <h1>{{ charts.metadata.name }} <small>- Chart display options</small></h1>
            <div class="panel panel-default">
                <div class="panel-heading">

                </div>
                <div class="panel-body">
                    <p>
                        Configure the chart to show for your dataset.<br/>
                        You can drag and drop the table to adjust the order the charts are presented.
                        <br/>
                        Please click save to store your configuration for this dataset.
                    </p>
                    <form>
                        <div class="pull-right">
                            <button class="btn btn-primary" type="button"ng-click="charts.save()">Save</button>
                        </div>

                        <table class="table table-condensed table-striped">
                            <thead>
                            <tr>
                                <th></th>
                                <th>Field name</th>
                                <th>Chart type</th>
                                <th>Show</th>
                            </tr>
                            </thead>
                            <tbody class="customIndexes" ui-sortable ng-model="charts.chartConfig">
                            <tr ng-repeat="chartCfg in charts.chartConfig">
                                <td style="cursor: ns-resize;"><i class="fa fa-reorder"></i></td>
                                <td ng-bind="chartCfg.formattedField"></td>
                                <td>
                                    <select name="format" ng-model="chartCfg.format">
                                        <option value="pie">Pie</option>
                                        <option value="bar">Bar</option>
                                        <option value="column">Column</option>
                                        <option value="line">Line</option>
                                        <option value="scatter">Scatter</option>
                                    </select>
                                </td>
                                <td>
                                    <input type="checkbox" ng-model="chartCfg.visible" />
                                </td>
                            </tr>
                            </tbody>
                        </table>
                        <div class="pull-right">
                            <button class="btn btn-primary" type="button" ng-click="charts.save()">Save</button>
                            <a class="btn btn-default" ui-sref="userDatasets({userId: charts.sandboxConfig.userId})">Cancel</a>
                        </div>

                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
</script>