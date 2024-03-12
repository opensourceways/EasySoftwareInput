# 1 目的
1. 服务的目的是解析精品应用，写入mysql数据表。
2. 精品应用来源于仓库`https://gitee.com/openeuler/openeuler-docker-images`，以`redis`为例
   1. 精品应用的字段来自于`https://gitee.com/openeuler/openeuler-docker-images/blob/master/redis/doc/image-info.yml`
   2. 图片来自于目录`https://gitee.com/openeuler/openeuler-docker-images/tree/master/redis/doc/picture`

# 2 程序
1. 程序有1个功能：
   1. 根据仓库更新数据库
2. `src\main\java\com\example\service\epkgpkg\ExecuteService.java`:向easysoftwareservice服务发送post请求
3. `src\main\java\com\example\service\epkgpkg\GitRepo.java`:拉取远程gitee仓库，寻找`image-info.yal`
4. `src\main\java\com\example\service\epkgpkg\ParseAppPkg.java`:解析`image-info.yml`