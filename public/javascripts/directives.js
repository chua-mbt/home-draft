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
    scope: { draft: "=", participants: "=" },
    controller: function($scope, matches) {
      $scope.$watch('draft.state', function(newVal, oldVal){
        if(newVal != 'tournament') return;
        $scope.reload();
      });
      $scope.update = function(){
        matches.edit(
          { hash: $scope.draft.hash, round: 'current' }, $scope.matchups
        );
      }
      $scope.next = function(){
        $scope.update();
      }
      $scope.reload = function(){
        matches.query({ hash: $scope.draft.hash, round: 'current' }, function(response){
          $scope.matchups = response;
        });
      }

      $scope.swapSelected = function(matchup, idx){
        if(matchup.results[idx].sel){
          return "border-width: 2px; border-style: solid; border-color: red;"
        }else{
          return "";
        }
      }
      var toSwap = null; var toSwapIdx = null;
      $scope.swapSelect = function(matchup, idx){
        function setSwap(firstPicked, fpIdx){
          firstPicked.results[fpIdx].sel = true;
          toSwap = firstPicked; toSwapIdx = fpIdx;
        }
        function clearSwap(){
          delete toSwap.results[toSwapIdx].sel;
          toSwap = null; toSwapIdx = null;
        }
        function swap(matchup1, idx1, matchup2, idx2){
          clearSwap();
          var tmp = matchup.results[idx1];
          matchup1.results[idx1] = matchup2.results[idx2];
          matchup2.results[idx2] = tmp;
          fixLosses(matchup1); fixLosses(matchup2);
          function fixLosses(toFix){
            toFix.results[0].losses = toFix.results[1].wins
            toFix.results[1].losses = toFix.results[0].wins
          }
        }
        if(toSwap){
          if (matchup != toSwap){
            swap(matchup, idx, toSwap, toSwapIdx);
          }else if(idx == toSwapIdx){
            clearSwap();
          }
        }else{
          setSwap(matchup, idx);
        }
      }
    }
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