# 1 目的
1. 服务的目的是解析rpm软件包，写入mysql数据表
2. epkg软件包的元信息（即软件包的名称、版本、架构、发布时间等信息）：`https://repo.openeuler.org/`。以`openEuler-20.03-LTS`为例：
    1. `/repodata/xxxprimary.xml`中存储的是rpm包元信息。例子：`https://repo.openeuler.org/openEuler-20.03-LTS/update/source/repodata/982b6c9aeae688e7fd113b081fd4e87e034956c5dae4241272cffb0ac6c2ff6b-primary.xml.gz`
    2. `/Pacakges`目录下是软件包实体。例子：`https://repo.openeuler.org/openEuler-20.03-LTS/update/source/Packages/`

# 2 服务
1. 服务功能：
    1. 解析 /repodata/primary.xml
    2. 解析rpm软件包中哪些属于源码包。rpm软件包可以分为二进制包和源码包。源码包包含源代码，以`.src.rpm`结尾。二进制包是源码包在特定架构下编译得到的可运行二进制包，以`.rpm`结尾。
    3. 解析src-openeuler仓库的所有包
2. `src\main\java\com\example\service\ParseXml.java`:软件包元信息的存储文件为`xxx.xml`，解析存储文件及每个软件包对应的`<package>`标签。
3. `src\main\java\com\example\service\ParseSrcPkg.java`:解析rpm软件包中哪些属于源码包
5. `src\main\java\com\example\service\ParseRepoType.java`:获取`https://gitee.com/src-openeuler`组织下的一万多个仓库的名称
6. `src\main\java\com\example\converter\PkgConverter.java`：将键值对对象写入java对象。
7. `src\main\java\com\example\service\ExecuteService.java`:发送post请求。
    