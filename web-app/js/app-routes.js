(function sandboxRoutes(config) {
  "use strict";
  var sandbox = angular.module('ala.sandbox');

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
          .state("preview", {
            controller: 'PreviewCtrl as preview',
            url: "/dataCheck?reload",
            templateUrl: 'preview.html',
            resolve: {
              existing: ['previewService', '$stateParams',
                function (previewService, $stateParams) {
                  if ($stateParams.reload) {
                    return previewService.reload($stateParams.reload)
                  } else {
                    return null;
                  }
                }]
            }
          });
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
        });
      }
    ]
  );
})(SANDBOX_CONFIG);