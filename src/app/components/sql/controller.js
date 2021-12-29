angular.module('cerebro').controller('SqlController', ['$scope', '$http',
  '$sce','$compile', 'SqlDataService', 'AlertService', 'ModalService',
  'AceSqlEditorService', 'ClipboardService','AceEditorService',
  function($scope, $http, $sce, $compile, SqlDataService, AlertService,
           ModalService, AceSqlEditorService, ClipboardService, AceEditorService) {

    $scope.editor = undefined;
    //$scope.response = undefined;
    // 存储返回来的文本数据
    $scope.responseObj = undefined;
    //ace json editor 存储全部结果
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

    var emptyExplain = JSON.stringify(
      {},
      undefined,
      2
    );

    var success = function(response) {
      console.log(response);
      if (response.explain) {
        $scope.explainText = JSON.stringify(response.explain, undefined, 2);
        $scope.explain.setValue($scope.explainText);
        $scope.explainHead = response.explainHead;
      }
      delete response.explain;
      delete response.explainHead;
      // if (typeof response == 'string' && response.startsWith("{")) {
      //   $scope.responseObj = JSON.stringify(response, undefined, 2);
      // } else {
      //   $scope.responseObj = response;
      // }
      $scope.responseObj = JSON.stringify(response, undefined, 2);
      $scope.rawReuslt.setValue($scope.responseObj);
      $scope.resultList = response.result;
      $scope.resultKeys = Object.keys($scope.resultList);
      $scope.loadHistory();
    };

    var failure = function(response) {
      $scope.rawReuslt.setValue(JSON.stringify(response, undefined, 2));
    };

    $scope.execute = function() {
      var data = $scope.editor.getStringValue();
      //var method = $scope.method;
      //$scope.rawReuslt = undefined;
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
      // explain json editor
      $scope.explain = AceEditorService.init('explain-editor');
      $scope.explain.setReadOnly(true);
      $scope.explain.setValue(emptyExplain);
      // rawReuslt
      $scope.rawReuslt = AceEditorService.init('raw-result-editor');
      $scope.rawReuslt.setReadOnly(true);
      $scope.rawReuslt.setValue(emptyExplain);
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
        angular.element('#raw-result-editor')[0].style.display = '';
        angular.element('#oneResult')[0].style.display = 'none';
      }else {
        var resultValue = $scope.resultList[result];
        $scope.showResult = resultValue.data;
        angular.element('#raw-result-editor')[0].style.display = 'none';
        angular.element('#oneResult')[0].style.display = '';
      }
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

    $scope.copyRawResult = function() {
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
