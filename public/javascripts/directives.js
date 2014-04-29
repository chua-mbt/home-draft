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
    controller: function(
      $scope, $location, $routeParams, mtgsets, drafts
    ) {
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
    scope: { draft: "=", participants: "=" }
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
    controller: function(
      $scope, $location, $routeParams, app_user, participants, enter_key
    ) {
      participants.query({ hash: $routeParams.hash }, function(response){
        $scope.participants = response;
      });
      $scope.toAddChanged = function($event){
        if($event.keyCode != enter_key){ return; }
        $scope.add($scope.toAdd);
      }
      $scope.add = function(handle){
        if(!handle || handle.length < 1) return;
        participants.save(
          { hash: $routeParams.hash },
          { handle: handle },
          function(response){
            $scope.participants.push(response);
            $scope.toAdd = "";
          }
        );
      }
      $scope.remove = function(handle){
        participants.delete({
          hash: $routeParams.hash,
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
    }
  }
});