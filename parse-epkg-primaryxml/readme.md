# 1 目的
1. 服务的目的是解析epkg软件包，写入mysql数据表
2. epkg软件包的元信息（即软件包的名称、版本、架构、发布时间等信息）：`https://eulermaker.compass-ci.openeuler.openatom.cn/api/ems1/repositories/epkg-test/repodata/2e81f3c616399c50d5cbda843d4907b4650199d639814dc6ae43152ca819e9d5-primary.xml.gz`
3. epkg软件包的包实体：`https://eulermaker.compass-ci.openeuler.openatom.cn/api/ems1/repositories/epkg-test/Packages/`

# 2 服务
1. 服务功能：
    1. 解析 epkg的软件包
    2. 解析epkg软件包中哪些属于源码包。epkg软件包可以分为二进制包和源码包。源码包包含源代码，以`.src.rpm`结尾。二进制包是源码包在特定架构下编译得到的可运行二进制包，以`.rpm`结尾。
2. `src\main\java\com\example\service\epkgpkg\ParseEpkg.java`:软件包元信息的存储文件为`xxx.xml`，解析存储文件
3. `src\main\java\com\example\service\epkgpkg\ParseEpkgSrcPkg.java`:解析epkg软件包中哪些属于源码包
4. `src\main\java\com\example\service\epkgpkg\ParsePkgLabel.java`:软件包存储文件中有多个软件包的元信息，每个`<package>`标签代表 1个软件包。解析`<package>`标签。
5. `src\main\java\com\example\service\epkgpkg\ParseRepoType.java`:获取`https://gitee.com/src-openeuler`组织下的一万多个仓库的名称
6. `src\main\java\com\example\service\Assemble.java`：将键值对对象写入java对象。
7. `src/main/java/com/example/service/ExecuteService.java`:发送post请求。
    