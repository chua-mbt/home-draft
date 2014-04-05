angular.module('home-draft', [
  'ngRoute',
  'controllers'
]).config(['$routeProvider', function($routeProvider) {
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
}]);
