angular.module('home-draft', [
  'ngRoute',
  'home-draft.services',
  'home-draft.directives',
  'home-draft.controllers'
]).config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
  $routeProvider.
    when('/drafts', {
      templateUrl: mgr_partial("drafts"),
      controller: 'DraftsCtrl'
    }).
    when('/drafts/new', {
      templateUrl: mgr_partial("draft_new"),
      controller: 'DraftNewCtrl'
    }).
    when('/drafts/:hash', {
      templateUrl: mgr_partial("draft_mgr"),
      controller: 'DraftMgrCtrl'
    }).
    otherwise({
      redirectTo: '/drafts'
    });
  $locationProvider.html5Mode(true);
}]);
