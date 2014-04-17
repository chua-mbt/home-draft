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
controller('DraftNewCtrl', ['$scope', '$location', 'drafts',
function ($scope, $location, drafts) {
  $scope.save = function(draft){
    drafts.save(
      null, draft, function(response){
        $location.path('/drafts/'+response.hash);
      },
      function(){ alert("Something terrible happened..."); }
    );
  }
}]).
controller('DraftMgrCtrl', ['$scope', '$routeParams', 'drafts',
function ($scope, $routeParams, drafts) {
  $scope.save = function(draft){
    drafts.edit(
      { hash: $routeParams.hash }, draft, function(){
        alert("Saved");
      },
      function(){ alert("Something terrible happened..."); }
    );
  }
}]);