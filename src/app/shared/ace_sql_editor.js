function AceSqlEditor(target) {
  // ace sql editor
  ace.config.set('basePath', '/');
  this.editor = ace.edit(target);
  this.editor.setFontSize('10px');
  this.editor.setTheme('ace/theme/cerebro');
  this.editor.getSession().setMode('ace/mode/sql');
  this.editor.getSession().setTabSize(2); // 制表符
  this.editor.getSession().setUseWrapMode(true); //自动换行
  this.editor.setOptions({
    fontFamily: 'Monaco, Menlo, Consolas, "Courier New", monospace',
    fontSize: '12px'
  });

  // sets value and moves cursor to beggining
  this.setValue = function(value) {
    this.editor.setValue(value, 1);
    this.editor.gotoLine(0, 0, false);
  };

  this.getValue = function() {
    var content = this.editor.getValue();
    if (content.trim()) {
      return content;
    }
  };

  this.getStringValue = function() {
    return this.editor.getValue();
  };

  // formats the sql content
  this.format = function() {
    try {
      var content = this.getValue();
      if (content) {
        this.editor.format();
        console.log(this.getStringValue());
        this.editor.gotoLine(0, 0, false);
      }
    } catch (error) { // nothing to do
    }
  };

}
