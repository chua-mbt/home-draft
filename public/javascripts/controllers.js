angular.module(
  'controllers', []
).
controller('DraftsCtrl', ['$scope', 'drafts',
function ($scope, drafts) {
}]).
controller('DraftNewCtrl', ['$scope', 'mtgsets', 'drafts',
function ($scope, mtgsets, drafts) {
  $scope.draft = { hash: "new" };
  mtgsets.get(function(response){
    $scope.mtgsets = response.results;
  });
  $scope.save = function(){
    drafts.save($scope.draft, function(){}, function(){});
  }
}]).
controller('DraftViewCtrl', ['$scope',
function ($scope) {
}]);