(function datasets() {
  "use strict";
  var datasets = angular.module('ala.sandbox.datasets', ['ui.router']);

  datasets.factory('datasetService', ['$http', '$httpParamSerializer', 'sandboxConfig',
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


  datasets.controller('DatasetsCtrl', [ '$log', '$stateParams', 'datasets', 'datasetService', 'sandboxConfig',
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

  datasets.controller('ChartOptionsCtrl', [ '$state', '$stateParams', '$window', 'chartOptions', 'datasetService', 'sandboxConfig',
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

  datasets.controller('AllDatasetsCtrl', ['sandboxConfig', 'allDatasets', function(sandboxConfig, allDatasets) {
    var self = this;
    angular.extend(self, allDatasets);
    self.showUserUpload = function(userUpload) {
      return userUpload.webserviceUrl && userUpload.webserviceUrl.indexOf(sandboxConfig.biocacheServiceUrl) == 0;
    };
  }]);
})();