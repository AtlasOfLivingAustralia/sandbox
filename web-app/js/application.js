"use strict";
function sandbox(config) {
  var components = angular.module('ala.sandbox.components', []);

  components.directive('allowTab', function () {
    return {
      require: 'ngModel',
      link: function(scope, ele, attrs, c) {
        ele.bind('keydown keyup', function(e) {
          var val = this.value;
          if (e.keyCode === 9 && e.type === 'keydown') { // tab was pressed


            // get caret position/selection
            var start = this.selectionStart,
              end = this.selectionEnd;

            // set textarea value to: text before caret + tab + text after caret
            this.value = val.substring(0, start) + '\t' + val.substring(end);

            // put caret at right position again
            this.selectionStart = this.selectionEnd = start + 1;

            c.$setValidity('allowTab', true);

            e.preventDefault();

            // prevent the focus lose
            return false;

          }
          else if(e.keyCode !== 9 && e.type === 'keyup') {
            if(val === '') {
              c.$setValidity('allowTab', false);
            }
            else {
              c.$setValidity('allowTab', true);
            }
          }
        });
      }
    }
  });
  
  
	var sandbox = angular.module('ala.sandbox', ['ngAnimate', 'ngAria', 'ngTouch', 'ui.bootstrap', 'ui.router', 'ala.sandbox.components', 'chieffancypants.loadingBar', 'ngFileUpload']);

  sandbox.value('sandboxConfig', config);

  sandbox.run(
    [          '$rootScope', '$state', '$stateParams',
      function ($rootScope,   $state,   $stateParams) {

        // It's very handy to add references to $state and $stateParams to the $rootScope
        // so that you can access them from any scope within your applications.For example,
        // <li ng-class="{ active: $state.includes('contacts.list') }"> will set the <li>
        // to active whenever 'contacts.list' or one of its decendents is active.
        $rootScope.$state = $state;
        $rootScope.$stateParams = $stateParams;
      }
    ]
  );

  sandbox.config(['cfpLoadingBarProvider', function(cfpLoadingBarProvider) {
    cfpLoadingBarProvider.includeSpinner = false;
    cfpLoadingBarProvider.parentSelector = '#loading-bar-container';
  }]);

  sandbox.config(
    [          '$stateProvider', '$urlRouterProvider', '$locationProvider',
      function ($stateProvider,   $urlRouterProvider, $locationProvider) {

        $locationProvider.html5Mode(true);

        $urlRouterProvider
         // If the url is ever invalid, e.g. '/asdf', then redirect to '/' aka the home state
          .when('/my-data', '/datasets/'+config.userId)
          .otherwise('/dataCheck/index');


        // Use $stateProvider to configure your states.
        $stateProvider
          .state("preview", {
            controller: 'PreviewCtrl as preview',
            url: "/dataCheck/index",
            templateUrl: 'preview.html'
          })
          .state('dataset', {
            url: '/datasets/:userId',
            controller: 'DatasetCtrl as dataset',
            // Showing off how you could return a promise from templateProvider
            templateUrl: 'datasets.html'
          })
      }
    ]
  );

  sandbox.factory('previewService', ['$http', '$httpParamSerializer', 'sandboxConfig', 'Upload',
    function ($http, $httpParamSerializer, sandboxConfig, Upload) {
      function randomString(length) {
        var chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz'.split('');

        if (! length) {
          length = Math.floor(Math.random() * chars.length);
        }

        var str = '';
        for (var i = 0; i < length; i++) {
          str += chars[Math.floor(Math.random() * chars.length)];
        }
        return str;
      }

      return {
        uploadCsvFile: function(file) {
          file.upload = Upload.upload({
            url: sandboxConfig.uploadCsvUrl,
            data: { myFile: file }
          });
          return file.upload;
        },
        parseColumns : function(text, fileId, firstLineIsData) {
          var data = $httpParamSerializer({ rawData: text, firstLineIsData: firstLineIsData, fileId: fileId });
          return $http.post(sandboxConfig.parseColumnsUrl, data, { headers: { 'Content-Type': 'application/x-www-form-urlencoded'}});
        },
        processData: function(columnHeaders, firstLineIsData, text, fileId) {
          var data = $httpParamSerializer({ headers: columnHeaders, firstLineIsData: firstLineIsData, rawData: text, fileId: fileId });
          return $http.post(sandboxConfig.processDataUrl, data, { headers: { 'Content-Type': 'application/x-www-form-urlencoded'}});
        },
        uploadToSandbox: function(columnHeaders, firstLineIsData, text, fileId, datasetName, customIndexedFields) {
          var data = $httpParamSerializer({ headers: columnHeaders, firstLineIsData: firstLineIsData, rawData: text, fileId: fileId, datasetName: datasetName, customIndexedFields: customIndexedFields });
          return $http.post(sandboxConfig.uploadToSandboxUrl, data, { headers: { 'Content-Type': 'application/x-www-form-urlencoded'} });
        },
        pollUploadStatus: function(uid) {
          return $http.get(sandboxConfig.uploadStatusUrl, { params: { uid: uid , random: randomString(10)}, ignoreLoadingBar: true });
        }
      };
    }]);
  
  sandbox.factory('datasetService', ['$http', '$httpParamSerializer', 'sandboxConfig',
    function ($http, $httpParamSerializer, sandboxConfig) {
      return {

      };
    }]);

  sandbox.controller("PreviewCtrl", ['$log', '$timeout', '$uibModal', '$window', 'previewService',
    function($log, $timeout, $uibModal, $window, previewService) {
      var self = this;

      self.firstLineOptions = [
        // {value: '', label: 'Autodetect' },
        {value: true, label: 'Data'},
        {value: false, label: 'Column headers'}
      ];

      self.fileId = null;
      self.fileName = null;
      self.uploadingCsv = false;

      self.text = '';
      self.file = null;
      self.parsing = false;
      self.preview = {
        firstLineIsData: null
      };
      self.previewLoaded = false;
      self.previewError = false;

      self.processingData = false;
      self.processedData = {};
      self.procssedDataError = true;
      self.datasetName = 'My test dataset';

      self.uploading = false;
      self.uploadPercent = 0;
      self.uploadFailed = false;

      self.dataResourceUid = null;
      self.datasetName = null;

      function reset() {
        self.preview = {
          firstLineIsData: null
        };
        self.previewLoaded = false;

        self.processingData = false;
        self.processedData = {};
        self.previewError = false;
        self.processedDataError = false;

        self.uploading = false;
        self.uploadPercent = 0;
        self.uploadFailed = false;
      }

      self.uploadCsvFile = function() {
        reset();
        self.uploadingCsv = true;
        var p = previewService.uploadCsvFile(self.file);
        p.then(function (response) {
          angular.extend(self, response.data);
          self.parseColumns();
        }, function(error) {
          $log.error("File upload failed", error);
        }).finally(function() {
          self.uploadingCsv = false;
        });
      };

      self.parseColumns = function() {
        reset();
        if (self.text || self.fileId) {
          self.parsing = true;
          var p = previewService.parseColumns(self.text, self.fileId, self.preview.firstLineIsData);
          p.then(function(response) {
            angular.extend(self.preview, response.data);
            self.previewLoaded = true;
            self.getProcessedData();
          }, function(error) {
            $log.error("Error getting parsed columns", error);
            self.previewError = true;
          }, function(notify) {
            $log.debug('notify parseColumns', notify);
          }).finally(function() {
            self.parsing = false;
          });
        }
      };

      function columnHeaders() {
        return _.pluck(self.preview.headers, 'header');
      }

      self.getProcessedData = function() {
        self.processingData = true;
        self.processedDataError = false;
        self.processedData = {};
        var p = previewService.processData(columnHeaders(), self.preview.firstLineIsData, self.text, self.fileId);
        p.then(function(response) {
          angular.extend(self.processedData, response.data);
        }, function(error) {
          $log.error("Error getting processed data", error);
          self.processedDataError = true;
        }, function(notify) {
          $log.debug('notify processData', notify);
        }).finally(function() {
          self.processingData = false;
        });
      };

      self.uploadToSandbox = function() {
        $log.info('Uploading to sandbox...');
        self.uploading = true;
        self.uploadPercent = 0;
        self.uploadFailed = false;
        var p = previewService.uploadToSandbox(columnHeaders(), self.preview.firstLineIsData, text, self.fileId, self.datasetName, null);
        p.then(function(response) {
          self.dataResourceUid = response.data.uid;
          updateStatusPolling();
        }, function(error) {
          if ( error.status == 401 ) {
            var isAuthenticated = error.headers("X-Sandbox-Authenticated");
            var isAuthorised = error.headers("X-Sandbox-Authorised");
            var template = !isAuthenticated ? 'notAuthenticatedModal.html' : 'notAuthorisedModal.html';
            $uibModal.open({
              templateUrl: template
            });
          } else {
            $window.alert('Fail:' + error.status);
          }
        });
      };

      function updateStatusPolling() {

        var p  = previewService.pollUploadStatus(self.dataResourceUid);

        p.then(function (response) {
          var data = response.data;
          $log.info("Retrieving status...." + data.status + ", percentage: " + data.percentage);
          if (data.status == "COMPLETE") {
            self.uploadPercent = 100;
          } else if (data.status == "FAILED") {
            self.uploadFailed = true;
          } else {
            self.uploadPercent = data.percentage;
            self.uploadStatus = data.status;
            self.uploadDescription = data.description;
            $timeout(updateStatusPolling, 1000);
          }
        });
      }

      self.checkDataLabel = function() {
        return self.parsing ? 'Loading...' : 'Check data';
      };

      self.uploadCsvStatusLabel = function() {
        return self.uploadingCsv ? 'Uploading...' : self.parsing ? 'Loading...' : 'Upload file';
      };

      self.reprocessDataLabel = function() {
        return self.parsing ? 'Reprocessing...' : 'Reprocess data';
      };


      self.isHeaderUnknown = function(header) {
        var searchString = 'Unknown ';
        return header.substr(0, searchString.length) === searchString;
      };

      self.processedRecordFieldClass = function(field) {
        return field.name == "informationWithheld" || field.name == "dataGeneralizations" ? "sensitiveField" : "fieldName";
      };

      self.processedRecordChangedClass = function(field) {
        return field.processed != field.raw && field.processed != null ? 'changedValue' : 'originalConfirmed';
      };

      self.countByQaStatus = function(status) {
        var count = 0;
        angular.forEach(processedData.assertions, function (it) {
          if(it.qaStatus == status){
            count++
          }
        });
        return count;
      };
    }]);

  sandbox.controller('DatasetCtrl', [ '$stateParams', function($stateParams) {
    
  }]);
}