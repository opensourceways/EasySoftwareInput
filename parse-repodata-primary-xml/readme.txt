1. springboot项目的目的是读取/repodata/primary.xml中rpm包元信息
2. rpm包元信息格式见\primary-xml-sample\openEuler-22.03-LTS-SP1_a_EPOL_a_update_a_main_a_source_a_438d1f63d3594ea7fe32fc6b708ccf7687f1d7f1f30b7b55818511012eee2117-primary.xml
3. 主要流程在\src\main\java\com\example\service\ParseXml.java
4. 发送post请求在l\src\main\java\com\example\service\ExecuteService.java