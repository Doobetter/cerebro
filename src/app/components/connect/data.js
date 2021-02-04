angular.module('cerebro').factory('ConnectDataService', ['$http', 'DataService',
  function($http, DataService) {

    this.getHosts = function(success, error) {
      var config = {method: 'GET', url: 'connect/hosts'};
      var handleSuccess = function(data) {
        if (data.status >= 200 && data.status < 300) {
          success(data.body);
        } else {
          error(data.body);
        }
      };
      $http(config).success(handleSuccess).error(error);
    };

    this.testConnection = function(host, success, error) {
      var config = {method: 'POST', url: 'connect', data: {host: host}};
      $http(config).success(success).error(error);
    };

    this.testConnectionWithCredentials = function(host, username, password, success, error) {
      var data = {host: host, username: username, password: password};
      var config = {method: 'POST', url: 'connect', data: data};
      $http(config).success(success).error(error);
    };

    this.testCredentials = function(host, username, password, success, error) {
      var data = {host: host, username: username, password: password};
      var config = {method: 'POST', url: 'connect', data: data};
      $http(config).success(success).error(error);
    };

    this.connect = function(host) {
      DataService.setHost(host);
    };

    this.addAndConnect = function(host, username, password, clusterName, sqlUrl) {
      var data = {host: host, username: username, password: password, name: clusterName, sqlUrl: sqlUrl};
      var config = {method: 'POST', url: 'hosts/save', data: data};
      var handleSuccess = function(data) {
        console.log(data);
        DataService.setHost(host, username, password);
      };
      var failure = function(data) {
        AlertService.error('Error save cluster ');
      };
      $http(config).success(handleSuccess).error(failure);
    };

    this.addAndConnectWithCredentials = function(host, username, password, clusterName, sqlUrl) {
      var data = {host: host, username: username, password: password, name: clusterName, sqlUrl: sqlUrl};
      var config = {method: 'POST', url: 'hosts/save', data: data};
      var handleSuccess = function(data) {
        console.log(data);
        DataService.setHost(host, username, password);
      };
      var failure = function(data) {
        AlertService.error('Error save cluster ');
      };
      $http(config).success(handleSuccess).error(failure);
    };

    this.connectWithCredentials = function(host, username, password) {
      DataService.setHost(host, username, password);
    };

    return this;

  }
]);
