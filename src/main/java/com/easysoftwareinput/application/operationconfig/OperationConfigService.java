package com.easysoftwareinput.application.operationconfig;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.javassist.bytecode.Opcode;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import com.easysoftwareinput.application.epkgpackage.EPKGPackageService;
import com.easysoftwareinput.common.entity.MessageCode;
import com.easysoftwareinput.common.entity.ResultVo;
import com.easysoftwareinput.common.utils.HttpClientUtil;
import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.domain.operationconfig.model.OpCo;
import com.easysoftwareinput.infrastructure.operationconfig.OpCoGatewayImpl;
import com.easysoftwareinput.infrastructure.operationconfig.converter.OpCoConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;

@Service
public class OperationConfigService {
    private static final Logger logger = LoggerFactory.getLogger(EPKGPackageService.class);

    @Value("${operation-config.path}")
    private String repoPath;

    @Value("${operation-config.url}")
    private String postUrl;

    @Value("${operation-config.truncate-url}")
    private String truncateUrl;

    @Value("${operation-config.redis-deletekey-url}")
    private String deleteKeyUrl;

    @Autowired
    OpCoGatewayImpl gateway;

    public void run() {
        gitPull(repoPath);
        String yamlPath = getYamlPath(repoPath);
        if (StringUtils.isBlank(yamlPath)) {
            logger.error("not repo: {}", repoPath);
            return;
        }

        Map<String, Object> map = parseYaml(yamlPath);
        List<String> rote =  parseRote(map);
        Map<String, List<String>> recommends = praseCategoryRecommend(map);
        List<OpCo> opCos = OpCoConverter.toEntity(rote, recommends);
        
        gateway.saveAll(opCos);
        
        logger.info("Finish-operation-config");
    }

    private String getYamlPath(String path) {
        Optional<String> opS = null;
        try (Stream<Path> pathS = Files.walk(Paths.get(path))) {
            opS = pathS.map(p -> p.toString()).filter(s -> s.endsWith("easySoftwareDomainConfig.yaml"))
                    .findFirst();
        } catch (Exception e) {
            logger.error("error to get config.yaml");
            return null;
        }

        String cPath = opS.orElseGet(null);
        if (StringUtils.isBlank(cPath)) {
            logger.error("no path of config.yaml");
            return null;
        }
        return cPath;
    }

    private void gitPull(String path) {
        try {         
            Git git = Git.open(new File(path));
            git.pull().call();
            git.close();
        } catch (Exception e) {
            logger.error("git pull exception", e);
        }
    }

    private void resetRedis() {
        HttpClientUtil.getRequest(deleteKeyUrl);
    }

    private void post(List<OpCo> opCos, String url) {
        for (OpCo opCo : opCos) {
            String body = ObjectMapperUtil.writeValueAsString(opCo);
            HttpClientUtil.postApp(url, body);
        }
    }

    private Map<String, List<String>> praseCategoryRecommend(Map<String, Object> map) {
        Map<String, List<String>> res = new HashMap<>();
        Map<String, Map<String, List<String>>> cateMap = null;
        try {
            cateMap = (Map<String, Map<String, List<String>>>) map.get("categorys");
        } catch (Exception e) {
            logger.info("Failed to parse category");
        }

        for (Map.Entry<String, Map<String, List<String>>> entry : cateMap.entrySet()) {
            String categorys = entry.getKey();
            Map<String, List<String>> vMap = entry.getValue();
            if (vMap.containsKey("recommend")) {
                List<String> recommend = vMap.get("recommend");
                res.put(categorys, recommend);
            }
        }
        return res;
    }


    private List<String> parseRote(Map<String, Object> map) {
        List<String> res = new ArrayList<>();
        try {
            res = (List<String>) map.get("sort");
        } catch (Exception e) {
            logger.info("Failed to parse rote");
        }
        return res;
    }

    private Map<String, Object> parseYaml(String yamlPath) {
        Yaml yaml = new Yaml();

        InputStream inputStream = null;
        Map<String, Object> map = new HashMap<>();
        try {
            inputStream = new FileInputStream(yamlPath);
            map = yaml.load(inputStream);
        } catch (FileNotFoundException e) {
            logger.error(MessageCode.EC0009.getMsgEn(), yamlPath);
        }
        
        return map;
    }
}
