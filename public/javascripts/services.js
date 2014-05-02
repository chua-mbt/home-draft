angular.module(
  'home-draft.services', ['ngResource']
).
factory('mtgsets',
function ($resource) {
  return $resource('/common/mtgsets');
}).
factory('draft_states', ['$resource',
function ($resource) {
  return $resource('/manager/draft-states/:name');
}]).
factory('drafts', ['$resource',
function ($resource) {
  return $resource('/manager/drafts/:hash', null, {'edit': { method:'PUT' }});
}]).
factory('draft_state', ['$resource',
function ($resource) {
  return $resource('/manager/drafts/:hash/state/:transition', null, {'edit': { method:'PUT' }});
}]).
factory('seats', ['$resource',
function ($resource) {
  return $resource('/manager/drafts/:hash/seats', null, {'edit': { method:'PUT', isArray:true }});
}]).
factory('matches', ['$resource',
function ($resource) {
  return $resource('/manager/drafts/:hash/matches/rounds/:round', null,
    {
      'edit': { method:'PUT', isArray:true },
      'save': { method:'POST', isArray:true }
    });
}]).
factory('standings', ['$resource',
function ($resource) {
  return $resource('/manager/drafts/:hash/standings');
}]).
factory('participants', ['$resource',
function ($resource) {
  return $resource('/manager/drafts/:hash/participants/:handle');
}]);