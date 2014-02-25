angular.module('common.config', ['ngRoute'])
  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
      .when('/sellers', {controller: TestCtrl, templateUrl: common_partial('')})
  }]);