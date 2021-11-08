angular.module('cerebro').controller('NavbarController', ['$scope', '$http',
  'PageService', 'DataService', 'RefreshService',
  function($scope, $http, PageService, DataService, RefreshService) {

    $scope.status = undefined;
    $scope.cluster_name = undefined;
    $scope.host = undefined;
    $scope.username = undefined;
    $scope.refreshInterval = RefreshService.getInterval();
    $scope.isAdmin = false;

    $scope.setRefreshInterval = function(interval) {
      RefreshService.setInterval(interval);
      $scope.refreshInterval = interval;
    };

    $scope.disconnect = function() {
      $scope.status = undefined;
      $scope.cluster_name = undefined;
      $scope.host = undefined;
      $scope.username = undefined;
      $scope.isAdmin = false;
      DataService.disconnect();
    };

    $scope.$watch(
      function() {
        return RefreshService.lastUpdate();
      },
      function() {
        DataService.getNavbarData(
          function(data) {
            $scope.status = data.status;
            $scope.cluster_name = data.cluster_name;
            $scope.username = data.username;
            console.log(data.username);
            if (data.username == 'sql') {
              $scope.isAdmin = false;
            } else {
              $scope.isAdmin = true;
            }
            console.log($scope.isAdmin);

            $scope.host = DataService.getHost();
            PageService.setup($scope.cluster_name, $scope.status);
          },
          function(error) {
            $scope.status = undefined;
            $scope.cluster_name = undefined;
            $scope.host = undefined;
            $scope.isAdmin = true;
            PageService.setup();
          }
        );
      }
    );

  }
]);
