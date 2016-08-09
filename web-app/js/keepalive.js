(function keepalive() {
  "use strict";
  var ka = angular.module('ala.sandbox.keepalive', ['ui.bootstrap']);
  ka.factory('keepaliveService', ['$http', 'sandboxConfig',
    function ($http, sandboxConfig) {
      return {
        keepalive: function () {
          return $http.get(sandboxConfig.keepaliveUrl, { ignoreLoadingBar: true });
        }
      }
    }]);
  ka.run(['$interval', '$uibModal', 'keepaliveService', function($interval, $uibModal, keepaliveService) {
    var kaPromise;
    function ka() {
      keepaliveService.keepalive().catch(function(response) {
        if (response.status > 299) {
          $interval.cancel(kaPromise);
          var modal = $uibModal.open({
            templateUrl: 'loggedOut.html'
          });
          modal.closed.then(function() {
            kaPromise = $interval(ka, 5000, 0, false);
          });
        }
      });
    }
    kaPromise = $interval(ka, 5000, 0, false);
  }]);
})();