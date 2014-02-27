angular.module('home-draft', [
  'ngRoute',
  'controllers'
]).config(['$routeProvider', function($routeProvider) {
  $routeProvider.
    when('/drafts', {
      templateUrl: 'partials/drafts.html',
      controller: 'DraftsCtrl'
    }).
    otherwise({
      redirectTo: '/drafts'
    });
}]);
