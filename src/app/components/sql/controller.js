angular.module('cerebro').controller('SqlController', ['$scope', '$http',
  '$sce','$compile', 'SqlDataService', 'AlertService', 'ModalService',
  'AceSqlEditorService', 'ClipboardService',
  function($scope, $http, $sce, $compile, SqlDataService, AlertService,
           ModalService, AceSqlEditorService, ClipboardService) {

    $scope.editor = undefined;
    $scope.response = undefined;
    // 存储返回来的文本数据
    $scope.responseObj = undefined;
    $scope.rawReuslt = undefined;
    $scope.resultList = undefined;
    // 当前的结果
    $scope.showResult = undefined;
    $scope.resultKeys = undefined;
    // elastic-sql-url eg.
    $scope.url = undefined;
    $scope.history = undefined;
    $scope.explain = undefined;
    $scope.explainText = undefined;
    $scope.explainHead = undefined;

    var success = function(response) {

      if (response.explain) {
        $scope.explain = $sce.trustAsHtml(JSONTree.create(response.explain));
        $scope.explainText = JSON.stringify(response.explain, null, 2);
        $scope.explainHead = response.explainHead;
      }
      delete response.explain;
      delete response.explainHead;
      $scope.responseObj = JSONTree.create(response);

      $scope.rawReuslt = $sce.trustAsHtml($scope.responseObj);
      //$scope.rawReuslt = $scope.responseObj;
      $scope.response = $scope.rawReuslt;
      $scope.resultList = response.result;
      $scope.resultKeys = Object.keys($scope.resultList);
      $scope.loadHistory();
    };

    var failure = function(response) {
      $scope.response = $sce.trustAsHtml(JSONTree.create(response));
    };

    $scope.execute = function() {
      var data = $scope.editor.getStringValue();
      //var method = $scope.method;
      $scope.rawReuslt = undefined;
      $scope.response = undefined;
      $scope.resultList = undefined;
      $scope.showResult = undefined;

      try {
        data = $scope.editor.getValue();
      } catch (error) {
      }
      // SqlDataService.execute(method, data, success, failure);
      SqlDataService.execute(data, success, failure);
    };

    // 初始化
    $scope.setup = function() {
      $scope.editor = AceSqlEditorService.init('sql-client-editor');

      SqlDataService.load(
        function(response) {
          $scope.url = response.url;
        },
        function(error) {
          AlertService.error('Error when load all the tables/indexes', error);
        }
      );
      var history = $scope.loadHistory();

    };

    $scope.loadRequest = function(request) {
      $scope.method = request.method;
      $scope.editor.setValue(request.body);
      $scope.editor.format();
      // load and collapse
      var link  = document.getElementById('link_sql_history');
      link.click();
    };

    $scope.loadHistory = function() {
      SqlDataService.history(
        function(history) {
          $scope.history = history;
          // edit中显示最后一次查询
          // console.log(history);
          var editorText =  $scope.editor.getValue();
          // console.log(editorText);
          if (editorText === undefined) {
            // 如果为第一次查询
            if (history !== undefined && history[0] !== undefined) {
              $scope.editor.setValue(history[0].body);
            }else {
              $scope.editor.setValue('SELECT * FROM my_index ');
            }
          }else {
            // do nothing
          }

          return history;
        },
        function(error) {
          AlertService.error('Error while loading request history', error);
          return undefined;
        }
      );
    };

    $scope.show = function(result) {
      if (1 === result) {
        var raw = $compile($scope.responseObj)($scope);
        angular.element('#result').html(raw);
      }else {
        var resultValue = $scope.resultList[result];
        $scope.showResult = resultValue.data;
        var ele = $compile($scope.fillTable())($scope);
        angular.element('#result').html(ele);
      }
    };

    $scope.fillTable = function() {
      var template = '<table class="table table-bordered">' +
        '    <thead>' +
        '      <tr>' +
        '        <th ng-repeat="(header, value) in showResult[0]">' +
        '          {{header}}</th>' +
        '      </tr>' +
        '    </thead>' +
        '    <tbody>' +
        '      <tr ng-repeat="row in showResult">\n' +
        '        <td ng-repeat="(key, value) in row">\n' +
        '          {{value}}\n' +
        '        </td>' +
        '      </tr>' +
        '    </tbody>' +
        '</table>';
      return template;
    };

    $scope.copyAsCURLCommand = function() {
      var path = encodeURI($scope.path);
      if (path.substring(0, 1) !== '/') {
        path = '/' + path;
      }
      var body = $scope.editor.getValue();
      var curl = 'curl -X POST \'' + $scope.url + '/a3' +
        '\' -H \'Content-Type: text/plain\'' +
        ' -d \'' + body + '\'';
      ClipboardService.copy(
        curl,
        function() {
          AlertService.info('cURL request successfully copied to clipboard');
        },
        function() {
          AlertService.error('Error while copying request to clipboard');
        }
      );
    };

    $scope.copyExplain = function() {
      ClipboardService.copy(
        $scope.explainText,
        function() {
          AlertService.info('Explain DSL successfully copied to clipboard');
        },
        function() {
          AlertService.error('Error while copying DSL to clipboard');
        }
      );
    };

  }]
);
