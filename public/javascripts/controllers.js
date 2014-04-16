angular.module(
  'home-draft.controllers', []
).
controller('DraftsCtrl', ['$scope', 'drafts', 'draft_states',
function ($scope, drafts, draft_states) {
  $scope.fetch = function(){
    if($scope.state == "all"){
      drafts.query(function(response){
        $scope.drafts = response;
      });
    }else{
      draft_states.query({ name: $scope.state }, function(response){
        $scope.drafts = response;
      });
    }
  }

  $scope.state = "all";
  $scope.dstates = [{ name: "all" }];
  draft_states.query(function(response){
    $scope.dstates = $scope.dstates.concat(response);
  });
  $scope.fetch();
}]).
controller('DraftNewCtrl', ['$scope', '$location',
function ($scope, $location) {
  $scope.onSave = function(){
    $location.path('/drafts');
  }
}]).
controller('DraftViewCtrl', ['$scope',
function ($scope) {
}]);