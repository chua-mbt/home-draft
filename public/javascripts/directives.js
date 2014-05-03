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
    controller: function($scope, $timeout, $document, draft_state, seats) {
      var beep = document.createElement('audio');
      beep.src = jsRoutes.manager.controllers.Assets.at("sounds/beep.mp3").url;

      var PACKS = 3;
      var PICKS = 15;
      var TIME = 60;

      $scope.drafting = {
        pack: 1,
        pick: 1,
        time: TIME,
        counter: null,
        on: function(){ return this.counter; },
        direction: function(){ return (this.pack%2?"LEFT":"RIGHT"); },
        stop: function(){
          if(this.counter){
            $timeout.cancel(this.counter)
            this.counter = null;
          }
        },
        flipSwitch: function(){
          if(this.counter){
            $timeout.cancel(this.counter)
            this.counter = null;
          }else{
            function tick(){
              $scope.drafting.time--;
              if($scope.drafting.time <=0 ){
                $scope.drafting.forward();
                $scope.drafting.time = TIME;
                beep.play();
              }
              $scope.drafting.counter = $timeout(tick, 1000);
            }
            $scope.drafting.counter = $timeout(tick, 1000);
          }
        },
        back: function(){
          this.time = TIME; this.stop();
          if(this.pick == 1 && this.pack == 1) return;
          this.pick--; if(this.pick < 1){
            this.pick = PICKS;
            this.pack--;
          }
        },
        forward: function(){
          this.time = TIME; this.stop();
          if(this.pick == PICKS && this.pack == PACKS) return;
          this.pick++; if(this.pick > PICKS){
            this.pick = 1;
            this.pack++;
          }
        }
      };

      function changeState(newState){
        draft_state.edit(
          { hash: $scope.draft.hash, transition: newState }, {}, function(response){
            $scope.draft.state = response.name
          }
        );
      }
      $scope.done = function(){ changeState("next"); }
      $scope.cancel = function(){ changeState("previous"); }
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
    scope: { draft: "=" },
    controller: function($scope, $rootScope, matches, draft_state) {
      $scope.$watch('matchups', function(newVal, oldVal){
        if(!newVal) return;
        $rootScope.$broadcast("matchups_updated");
      });
      $scope.$watch('draft.state', function(newVal, oldVal){
        if(newVal != 'tournament'&&newVal != 'finished') return;
        matches.get({ hash: $scope.draft.hash }, function(response){
          $scope.round = $scope.rounds = response.rounds;
          $scope.reload();
        });
      });
      $scope.reload = function(){
        if($scope.round > $scope.rounds){ $scope.round = $scope.rounds; }
        if($scope.round < 1){ $scope.round = 1; }
        matches.query({ hash: $scope.draft.hash, round: $scope.round }, function(response){
          $scope.matchups = response;
        });
      }
      $scope.update = function(){
        matches.edit(
          { hash: $scope.draft.hash, round: 'current' }, $scope.matchups, function(response){
          $scope.matchups = response;
        });
      }
      $scope.next = function(){
        matches.save(
          { hash: $scope.draft.hash, round: 'current' }, $scope.matchups, function(response){
          $scope.rounds++;
          $scope.round = $scope.rounds;
          $scope.matchups = response;
        });
      }
      function changeState(newState){
        draft_state.edit(
          { hash: $scope.draft.hash, transition: newState }, {}, function(response){
            $scope.draft.state = response.name
          }
        );
      }
      $scope.cancel = function(){
        if($scope.rounds == 1){
          changeState("previous");
        }else{
          matches.delete(
            { hash: $scope.draft.hash, round: 'current' }, function(response){
            $scope.rounds--;
            $scope.round = $scope.rounds;
            $scope.matchups = response;
          });
        }
      }
      $scope.finish = function(){
        matches.edit(
          { hash: $scope.draft.hash, round: 'current' }, $scope.matchups, function(response){
          $scope.matchups = response;
          changeState("next");
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
directive('standings', function(){
  return {
    restrict: "E",
    templateUrl: mgr_partial("standings"),
    scope: { draft: "=" },
    controller: function($scope, standings) {
      $scope.$on('matchups_updated', function() {
        $scope.reload();
      });
      $scope.$watch('draft.state', function(newVal, oldVal){
        if(newVal != 'tournament'&&newVal != 'finished') return;
        $scope.reload();
      });
      $scope.reload = function(){
        standings.get({ hash: $scope.draft.hash }, function(response){
          $scope.rounds = response.rounds;
          $scope.cumulative = response.cumulative;
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