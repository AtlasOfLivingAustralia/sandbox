(function datasets() {
  "use strict";
  var sandbox = angular.module('ala.sandbox');

  sandbox.config(
    [          '$stateProvider',
      function ($stateProvider) {

        $stateProvider
          .state('userDatasets', {
            url: '/datasets/:userId',
            controller: 'DatasetsCtrl as datasets',
            templateUrl: 'datasets.html',
            resolve: {
              datasets: ['datasetService', '$q', '$stateParams',
                function(datasetService, $q, $stateParams) {
                  if (!$stateParams.userId) return $q.reject("No userId found");
                  return datasetService.getDatasetsForUser($stateParams.userId);
                }]
            }
          })
          .state('chartOptions', {
            url: '/charts/:tempUid',
            controller: 'ChartOptionsCtrl as charts',
            templateUrl: 'chartOptions.html',
            resolve: {
              chartOptions: ['datasetService', '$stateParams',
                function(datasetService, $stateParams) {
                  return datasetService.getChartOptions($stateParams.tempUid);
                }]
            }
          })
          .state('allDatasets', {
            url: '/datasets',
            controller: 'AllDatasetsCtrl as allDatasets',
            templateUrl: 'allDatasets.html',
            resolve: {
              allDatasets: ['datasetService',
                function(datasetService) {
                  return datasetService.getAllDatasets();
                }]
            }
          })
      }
    ]
  );

  sandbox.factory('datasetService', ['$http', '$httpParamSerializer', 'sandboxConfig',
    function ($http, $httpParamSerializer, sandboxConfig) {
      return {
        deleteResource: function(uid) {
          var data = $httpParamSerializer({ uid: uid });
          return $http.post(sandboxConfig.deleteResourceUrl, data, { headers: { 'Content-Type': 'application/x-www-form-urlencoded'} });
        },
        getAllDatasets: function() {
          return $http.get(sandboxConfig.getAllDatasetsUrl).then(function(response) { return response.data; });
        },
        getDatasetsForUser: function(userId) {
          return $http.get(sandboxConfig.getDatasetsUrl, { params: { userId: userId } }).then(function(response) { return response.data; });
        },
        getChartOptions: function(tempUid) {
          return $http.get(sandboxConfig.chartOptionsUrl, { params: { tempUid: tempUid } }).then(function(response) { return response.data; });
        },
        saveChartOptions: function(tempUid, chartOptions ) {
          return $http.post(sandboxConfig.saveChartOptionsUrl, chartOptions, { params: { tempUid: tempUid }});
        }
      };
    }]);


  sandbox.controller('DatasetsCtrl', [ '$log', '$stateParams', 'datasets', 'datasetService', 'sandboxConfig',
    function($log, $stateParams, datasets, datasetService, sandboxConfig) {
      var self = this;
      angular.extend(self, datasets);
      self.userId = $stateParams.userId;
      self.sandboxConfig = sandboxConfig;

      self.deleteSuccesses = [];
      self.deleteFailures = [];
      function isCurrentUser() {
        return self.userId == sandboxConfig.userId;
      }
      self.isCurrentUser = isCurrentUser;
      self.title = function() {
        return isCurrentUser() ? 'My uploaded datasets' : datasets.user.displayName + "'s datasets";
      };

      self.deleteResource = function(uid) {
        datasetService.deleteResource(uid).then(function(response) {
          if (response.data.success) {
            self.deleteSuccesses.push(uid);
            var idx = _.indexOf(self.userUploads, function(e) { e.uid == uid; });
            self.userUploads.splice(idx,1);
          } else {
            self.deleteFailures.push(uid);
          }
        }, function(error) {
          self.deleteFailures.push(uid);
        })
      };
    }]);

  sandbox.controller('ChartOptionsCtrl', [ '$state', '$stateParams', '$window', 'chartOptions', 'datasetService', 'sandboxConfig',
    function($state, $stateParams, $window, chartOptions, datasetService, sandboxConfig) {
      var self = this;
      self.saving = false;
      self.sandboxConfig = sandboxConfig;
      angular.extend(self, chartOptions);
      // self.chartConfig.push({ field: 'scientificName', formattedField: 'Scientific Name', format: 'bar', visible: false})
      self.save = function() {
        self.saving = true;
        datasetService.saveChartOptions(chartOptions.metadata.uid, self.chartConfig).then(function (response) {
          $state.go('userDatasets', {userId: sandboxConfig.userId })
        }, function (error) {
          $window.alert("Save failed :(");
        }).finally(function() {
          self.saving = false;
        });
      }
    }]);

  sandbox.controller('AllDatasetsCtrl', ['sandboxConfig', 'allDatasets', function(sandboxConfig, allDatasets) {
    var self = this;
    angular.extend(self, allDatasets);
    self.showUserUpload = function(userUpload) {
      return userUpload.webserviceUrl && userUpload.webserviceUrl.indexOf(sandboxConfig.biocacheServiceUrl) == 0;
    };
  }]);
})();