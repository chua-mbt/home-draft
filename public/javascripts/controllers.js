angular.module(
  'controllers', []
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
controller('DraftNewCtrl', ['$scope', '$location', 'mtgsets', 'drafts',
function ($scope, $location, mtgsets, drafts) {
  mtgsets.query(function(response){
    $scope.mtgsets = response;
  });
  $scope.save = function(){
    drafts.save($scope.draft, function(){
      $location.path('/drafts');
    }, function(){ console.log("Something terrible happened..."); });
  }
}]).
controller('DraftViewCtrl', ['$scope',
function ($scope) {
}]);