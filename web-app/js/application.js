(function sandbox(config) {
  "use strict";
	var sandbox = angular.module('ala.sandbox',
    ['ngAnimate', 'ngAria', 'ngTouch', 'ui.bootstrap', 'ui.router', 'ui.sortable',
      'ala.sandbox.components', 'ala.sandbox.datasets', 'ala.sandbox.preview',
      'chieffancypants.loadingBar', 'ngFileUpload']);

  sandbox.value('sandboxConfig', config);

  sandbox.config(['cfpLoadingBarProvider', function(cfpLoadingBarProvider) {
    cfpLoadingBarProvider.includeSpinner = false;
    // cfpLoadingBarProvider.parentSelector = '#loading-bar-container';
  }]);

})(SANDBOX_CONFIG);