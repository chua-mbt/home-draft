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
factory('participants', ['$resource',
function ($resource) {
  return $resource('/manager/drafts/:hash/participants/:handle');
}]);