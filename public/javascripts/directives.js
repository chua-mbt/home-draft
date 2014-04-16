angular.module(
  'home-draft.directives', []
).
directive('draftForm', function(){
  return {
    restrict: "E",
    templateUrl: mgr_partial("draft_form"),
    scope: { onSave: "=" },
    controller: function($scope, $location, mtgsets, drafts) {
      mtgsets.query(function(response){
        $scope.mtgsets = response;
      });
      $scope.save = function(){
        drafts.save(
          $scope.draft, $scope.onSave,
          function(){ console.log("Something terrible happened..."); }
        );
      }
    }
  }
});