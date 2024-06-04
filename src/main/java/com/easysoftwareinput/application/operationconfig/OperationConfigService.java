/* Copyright (c) 2024 openEuler Community
 EasySoftwareInput is licensed under the Mulan PSL v2.
 You can use this software according to the terms and conditions of the Mulan PSL v2.
 You may obtain a copy of Mulan PSL v2 at:
     http://license.coscl.org.cn/MulanPSL2
 THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 See the Mulan PSL v2 for more details.
*/

package com.easysoftwareinput.application.operationconfig;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import com.easysoftwareinput.application.epkgpackage.EPKGPackageService;
import com.easysoftwareinput.common.entity.MessageCode;
import com.easysoftwareinput.common.utils.HttpClientUtil;
import com.easysoftwareinput.common.utils.ObjectMapperUtil;
import com.easysoftwareinput.domain.operationconfig.ability.OpCoConverter;
import com.easysoftwareinput.domain.operationconfig.model.OpCo;
import java.io.File;

@Service
public class OperationConfigService {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EPKGPackageService.class);

    /**
     * path of repo.
     */
    @Value("${operation-config.path}")
    private String repoPath;

    /**
     * post url.
     */
    @Value("${operation-config.url}")
    private String postUrl;

    /**
     * url used to truncate table.
     */
    @Value("${operation-config.truncate-url}")
    private String truncateUrl;

    /**
     * url used to deleting keys.
     */
    @Value("${operation-config.redis-deletekey-url}")
    private String deleteKeyUrl;

    /**
     * run the grogram.
     */
    public void run() {
        gitPull(repoPath);
        String yamlPath = getYamlPath(repoPath);
        if (StringUtils.isBlank(yamlPath)) {
            return;
        }

        Map<String, Object> map = parseYaml(yamlPath);
        List<String> rote =  parseRote(map);
        Map<String, List<String>> recommends = praseCategoryRecommend(map);
        List<OpCo> opCos = OpCoConverter.toEntity(rote, recommends);

        HttpClientUtil.getRequest(truncateUrl);
        post(opCos, postUrl);
        resetRedis();

        LOGGER.info("Finished operation_config");
    }

    /**
     * get the yaml path of repo.
     * @param path path of repo.
     * @return path of yaml file.
     */
    private String getYamlPath(String path) {
        File folder = new File(path);
        String basePath = "";
        try {
            basePath = folder.getCanonicalPath();
        } catch (Exception e) {
            LOGGER.error("get yaml path exception", e);
        }
        String fullPath = Paths.get(basePath, "src", "openeuler", "easySoftwareDomainConfig.yaml").toString();

        File fullFile = new File(fullPath);
        if (!fullFile.exists() || !fullFile.isFile()) {
            LOGGER.error("{} does not have config.yaml", path);
            return "";
        }
        return fullPath;
    }

    /**
     * git pull from the repo.
     * @param path path of repo.
     */
    private void gitPull(String path) {
        try {
            Git git = Git.open(new File(path));
            git.pull().call();
            git.close();
        } catch (Exception e) {
            LOGGER.error("git pull exception", e);
        }
    }

    /**
     * reset the redis.
     */
    private void resetRedis() {
        HttpClientUtil.getRequest(deleteKeyUrl);
    }

    /**
     * post.
     * @param opCos list to be posted.
     * @param url url.
     */
    private void post(List<OpCo> opCos, String url) {
        for (OpCo opCo : opCos) {
            String body = ObjectMapperUtil.writeValueAsString(opCo);
            HttpClientUtil.postApp(url, body);
        }
    }

    /**
     * parse recommend.
     * @param map map from yaml file.
     * @return map of category and recommend.s
     */
    private Map<String, List<String>> praseCategoryRecommend(Map<String, Object> map) {
        Map<String, List<String>> res = new HashMap<>();
        Map<String, Map<String, List<String>>> cateMap = null;
        try {
            cateMap = (Map<String, Map<String, List<String>>>) map.get("categorys");
        } catch (Exception e) {
            LOGGER.info("Failed to parse category");
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

    /**
     * parse rote.
     * @param map map of yaml file.
     * @return list of sort.
     */
    private List<String> parseRote(Map<String, Object> map) {
        List<String> res = new ArrayList<>();
        try {
            res = (List<String>) map.get("sort");
        } catch (Exception e) {
            LOGGER.info("Failed to parse rote");
        }
        return res;
    }

    /**
     * convert yaml to map.
     * @param yamlPath path of yaml file.
     * @return map.
     */
    private Map<String, Object> parseYaml(String yamlPath) {
        Yaml yaml = new Yaml();

        InputStream inputStream = null;
        Map<String, Object> map = new HashMap<>();
        try {
            inputStream = new FileInputStream(yamlPath);
            map = yaml.load(inputStream);
        } catch (FileNotFoundException e) {
            LOGGER.error(MessageCode.EC0009.getMsgEn(), yamlPath);
        }
        return map;
    }
}
