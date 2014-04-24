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
      null, $scope.draft, function(response){
        $location.path('/drafts/'+response.hash);
      },
      function(){ alert("Something terrible happened..."); }
    );
  }
}]).
controller('DraftMgrCtrl', [
  '$scope', '$routeParams', 'drafts', 'draft_state', 'participants',
function ($scope, $routeParams, drafts, draft_state, participants) {
  drafts.get({ hash: $routeParams.hash }, function(response){
    $scope.draft = response;
  });
  $scope.changeState = function(newState, success){
    draft_state.edit(
      { hash: $routeParams.hash, transition: newState }, {}, function(response){
        $scope.draft.state = response.name
        if(success) { success(); }
      }
    );
  }
  $scope.begin = function(){ $scope.changeState("next", function(){
    participants.query({ hash: $routeParams.hash }, function(response){
      $scope.participants = response;
    });
  })}
  $scope.abort = function(){ $scope.changeState("abort") }
  $scope.save = function(){
    drafts.edit(
      { hash: $routeParams.hash }, $scope.draft, function(){
        alert("Saved");
      },
      function(){ alert("Something terrible happened..."); }
    );
  }
}]);