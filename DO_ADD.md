# 1 无ide开发
### 1.1 后端代码 

```
$sbt

> compile
> run
```

### 1.2 页面开发

首先安装依赖

grunt build

grunt watch

### 1.3 打包

```
# creates a zip file
grunt build
sbt universal:packageBin 

```

部署包生成位置

```
cerebro-master/target/universal/cerebro-0.8.5.zip
```

### 1.4 启动

```
bin/cerebro &>/dev/null &
```

# 2 IDEA 开发

参考 play framework 的一般开发过程
https://www.jianshu.com/p/190027067d85

