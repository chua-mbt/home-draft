angular.module(
  'services', ['ngResource']
).
factory('mtgsets',
function ($resource) {
  return $resource('/common/mtgsets');
}).
factory('drafts', ['$resource',
function ($resource) {
  return $resource('/manager/drafts/:hash');
}]);