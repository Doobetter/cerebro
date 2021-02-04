angular.module('cerebro').factory('AceSqlEditorService', function() {

  this.init = function(name) {
    return new AceSqlEditor(name);
  };

  return this;
});
