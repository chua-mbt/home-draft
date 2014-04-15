angular.module('home-draft', [
  'ngRoute',
  'services',
  'controllers'
]).config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
  $routeProvider.
    when('/drafts', {
      templateUrl: jsRoutes.manager.controllers.Assets.at("partials/drafts.html").url,
      controller: 'DraftsCtrl'
    }).
    when('/drafts/new', {
      templateUrl: jsRoutes.manager.controllers.Assets.at("partials/draft_new.html").url,
      controller: 'DraftNewCtrl'
    }).
    when('/drafts/:hash', {
      templateUrl: jsRoutes.manager.controllers.Assets.at("partials/draft_view.html").url,
      controller: 'DraftViewCtrl'
    }).
    otherwise({
      redirectTo: '/drafts'
    });
  $locationProvider.html5Mode(true);
}]);
