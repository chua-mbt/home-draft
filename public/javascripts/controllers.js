angular.module(
  'home-draft.controllers', []
).
controller('DraftsCtrl', ['$scope', 'drafts', 'draft_states',
function ($scope, drafts, draft_states) {
  $scope.offset = 0;
  $scope.drafts = [];
  $scope.fetch = function(){
    if($scope.state == "all"){
      drafts.query({
        start: $scope.offset
      }, function(response){
        $scope.drafts = $scope.drafts.concat(response);
      });
    }else{
      draft_states.query({
        name: $scope.state,
        start: $scope.offset
      }, function(response){
        $scope.drafts = $scope.drafts.concat(response);
      });
    }
  }
  $scope.more = function(){
    $scope.offset += 10;
    $scope.fetch();
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
  '$scope', '$routeParams', 'drafts', 'draft_state', 'draft_states', 'participants',
function ($scope, $routeParams, drafts, draft_state, draft_states, participants) {
  draft_states.query(function(response){
    $scope.dstateMap = {};
    response.map(
      function(state){
        $scope.dstateMap[state.name] = state.number;
      }
    );
  });
  drafts.get({ hash: $routeParams.hash }, function(response){
    $scope.draft = response;
  });
  $scope.inState = function(state){
    return (
      $scope.dstateMap && $scope.draft &&
      $scope.dstateMap[$scope.draft.state] == $scope.dstateMap[state]
    );
  }
  $scope.pastState = function(state){
    return (
      $scope.dstateMap && $scope.draft &&
      $scope.dstateMap[$scope.draft.state] > $scope.dstateMap[state] &&
      $scope.dstateMap[$scope.draft.state] != $scope.dstateMap['aborted']
    )
  }
  $scope.changeState = function(newState){
    draft_state.edit(
      { hash: $scope.draft.hash, transition: newState }, {}, function(response){
        $scope.draft.state = response.name
      }
    );
  }
  $scope.begin = function(){ $scope.changeState("next"); }
  $scope.abort = function(){ $scope.changeState("abort"); }
  $scope.save = function(){
    drafts.edit(
      { hash: $scope.draft.hash }, $scope.draft, function(){
        alert("Saved");
      },
      function(){ alert("Something terrible happened..."); }
    );
  }
}]);