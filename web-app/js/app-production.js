angular.module('ala.sandbox').config(['$compileProvider', function ($compileProvider) {
  $compileProvider.debugInfoEnabled(false);
}]).config(['$logProvider', function($logProvider) {
  $logProvider.debugEnabled(false);
}]);