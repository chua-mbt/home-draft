angular.module('home-draft', [
  'ngRoute',
  'controllers'
]).config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
  $routeProvider.
    when('/drafts', {
      templateUrl: jsRoutes.manager.controllers.Assets.at("partials/drafts.html").url,
      controller: 'DraftsCtrl'
    }).
    when('/drafts/:hash', {
      templateUrl: jsRoutes.manager.controllers.Assets.at("partials/draft.html").url,
      controller: 'DraftCtrl'
    }).
    otherwise({
      redirectTo: '/drafts'
    });
  $locationProvider.html5Mode(true);
}]);
