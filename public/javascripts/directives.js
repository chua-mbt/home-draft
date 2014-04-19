angular.module(
  'home-draft.directives', []
).
directive('draftForm', function(){
  return {
    restrict: "E",
    templateUrl: mgr_partial("draft_form"),
    scope: { save: "=" },
    controller: function(
      $scope, $attrs, $location, $routeParams, mtgsets, drafts
    ) {
      mtgsets.query(function(response){
        $scope.mtgsets = response;
      });
      if("edit" in $attrs){
        drafts.get({ hash: $routeParams.hash }, function(response){
          $scope.draft = response;
        });
      }
    }
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