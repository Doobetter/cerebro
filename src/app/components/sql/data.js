angular.module('cerebro').factory('SqlDataService', ['DataService',
  function(DataService) {

    this.load = function(success, error) {
      DataService.send('sql', {}, success, error);
    };

    this.history = function(success, error) {
      DataService.send('sql/history', {}, success, error);
    };

    this.execute = function(data, success, error) {
      var requestData = {data: data};
      DataService.send('sql/request', requestData, success, error);
    };

    this.getHost = function() {
      return DataService.getHost();
    };

    return this;
  }
]);
