(function sandbox(config) {
  "use strict";
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
  
  
	var sandbox = angular.module('ala.sandbox', ['ngAnimate', 'ngAria', 'ngTouch', 'ui.bootstrap', 'ui.router', 'ui.sortable', 'ala.sandbox.components', 'chieffancypants.loadingBar', 'ngFileUpload']);

  sandbox.value('sandboxConfig', config);

  sandbox.config(['cfpLoadingBarProvider', function(cfpLoadingBarProvider) {
    cfpLoadingBarProvider.includeSpinner = false;
    // cfpLoadingBarProvider.parentSelector = '#loading-bar-container';
  }]);

  sandbox.config(
    [          '$stateProvider', '$urlRouterProvider', '$locationProvider',
      function ($stateProvider,   $urlRouterProvider, $locationProvider) {

        $locationProvider.html5Mode({
          enabled: true,
          requireBase: true,
          rewriteLinks: false
        });

        $urlRouterProvider
          .when('/dataCheck/index', '/dataCheck')
          .when('/my-data', '/datasets/'+config.userId)
          .otherwise('/dataCheck');
      }
    ]
  );

  sandbox.run(
    [          '$rootScope', '$state', '$stateParams',
      function ($rootScope,   $state,   $stateParams) {

        // It's very handy to add references to $state and $stateParams to the $rootScope
        // so that you can access them from any scope within your applications.For example,
        // <li ng-class="{ active: $state.includes('contacts.list') }"> will set the <li>
        // to active whenever 'contacts.list' or one of its decendents is active.
        $rootScope.$state = $state;
        $rootScope.$stateParams = $stateParams;

        $rootScope.$on('$stateChangeError', function(event, toState, toParams, fromState, fromParams, error) {
          event.preventDefault();
          $state.go('preview');
          $
        });
      }
    ]
  );

})(SANDBOX_CONFIG);