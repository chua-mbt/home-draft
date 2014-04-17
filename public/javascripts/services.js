angular.module(
  'home-draft.services', ['ngResource']
).
factory('mtgsets',
function ($resource) {
  return $resource('/common/mtgsets');
}).
factory('drafts', ['$resource',
function ($resource) {
  return $resource('/manager/drafts/:hash', null, {'edit': { method:'PUT' }});
}]).
factory('draft_states', ['$resource',
function ($resource) {
  return $resource('/manager/draft-states/:name');
}]).
factory('participants', ['$resource',
function ($resource) {
  return $resource('/manager/drafts/:hash/participants/:handle');
}]);