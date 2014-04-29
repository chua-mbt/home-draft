angular.module(
  'home-draft.directives', []
).
directive('draftDetails', function(){
  return {
    restrict: "E",
    templateUrl: mgr_partial("draft_details"),
    scope: { draft: "=" }
  }
}).
directive('draftForm', function(){
  return {
    restrict: "E",
    templateUrl: mgr_partial("draft_form"),
    scope: { draft: "=", save: "=" },
    controller: function($scope, $location, mtgsets, drafts) {
      mtgsets.query(function(response){
        $scope.mtgsets = response;
      });
      $scope.$watch('draft.fee', function(newVal, oldVal){
        if(!newVal) return;
        $scope.draft.fee = parseFloat(newVal.toFixed(2));
      });
    }
  }
}).
directive('drafting', function(){
  return {
    restrict: "E",
    templateUrl: mgr_partial("drafting"),
    scope: { draft: "=", participants: "=" },
    controller: function($scope, draft_state, seats) {
      $scope.changeState = function(newState){
        draft_state.edit(
          { hash: $scope.draft.hash, transition: newState }, {}, function(response){
            $scope.draft.state = response.name
          }
        );
      }
      $scope.done = function(){ $scope.changeState("next"); }
      $scope.cancel = function(){ $scope.changeState("previous"); }
      $scope.shuffle = function(){
        seats.edit(
          { hash: $scope.draft.hash }, {}, function(response){
            $scope.participants = response;
          }
        );
      }
    }
  }
}).
directive('matchUps', function(){
  return {
    restrict: "E",
    templateUrl: mgr_partial("matchups"),
    scope: { draft: "=", participants: "=" }
  }
}).
directive('participants', function(){
  return {
    restrict: "E",
    templateUrl: mgr_partial("participants"),
    scope: { draft: "=", participants: "=" },
    controller: function($scope, $location, app_user, participants, enter_key) {
      $scope.toAddChanged = function($event){
        if($event.keyCode != enter_key){ return; }
        $scope.add($scope.toAdd);
      }
      $scope.add = function(handle){
        if(!handle || handle.length < 1) return;
        participants.save(
          { hash: $scope.draft.hash },
          { handle: handle },
          function(response){
            $scope.participants.push(response);
            $scope.toAdd = "";
          }
        );
      }
      $scope.remove = function(handle){
        participants.delete({
          hash: $scope.draft.hash,
          handle: handle
        }, function(response){
          $scope.participants = $scope.participants.filter(
            function(participant){
              return participant.user != handle;
            }
          );
          if(handle == app_user.handle){
            $location.path('/drafts');
          }
        });
      }
      $scope.$watch('draft', function(newVal, oldVal){
        if(!newVal) return;
        $scope.reload();
      });
      $scope.$watch('draft.state', function(newVal, oldVal){
        if(newVal != 'drafting') return;
        $scope.reload();
      });
      $scope.reload = function(){
        participants.query({ hash: $scope.draft.hash }, function(response){
          $scope.participants = response;
        });
      }
    }
  }
});