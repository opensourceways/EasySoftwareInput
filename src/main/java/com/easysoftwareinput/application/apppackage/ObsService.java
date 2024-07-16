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

package com.easysoftwareinput.application.apppackage;

import com.obs.services.ObsClient;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectRequest;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ObsService {
    /**
     * obs endpoint.
     */
    @Value("${obs.endpoint}")
    private String obsEndpoint;

    /**
     * bos bucket name.
     */
    @Value("${obs.bucket}")
    private String obsBucketName;

    /**
     * obs ak.
     */
    @Value("${obs.ak}")
    private String obsAk;

    /**
     * obs sk.
     */
    @Value("${obs.sk}")
    private String obsSk;

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ObsService.class);

    /**
     * obsclient.
     */
    private ObsClient obsClient;

    /**
     * init of obsclient.
     */
    @PostConstruct
    public void init() {
        obsClient =  new ObsClient(obsAk, obsSk, obsEndpoint);
    }

    /**
     * save the data to obs.
     * @param ObjectKey key of data.
     * @param filePath file path to be stored.
     */
    public void putData(String ObjectKey, String filePath) {
        if (filePath == null) {
            return;
        }
        PutObjectRequest request = new PutObjectRequest();
        request.setBucketName(obsBucketName);
        request.setObjectKey(ObjectKey);
        request.setFile(new File(filePath));
        obsClient.putObject(request);
        LOGGER.info("finish-push-pic, key: {}, path: {}", ObjectKey, filePath);
    }

    /**
     * get data by key.
     * @param ObjectKey key
     * @return a res.
     */
    public InputStream getData(String ObjectKey) {
        ObsObject object = obsClient.getObject(obsBucketName, ObjectKey);
        InputStream res = object.getObjectContent();
        return res;
    }

    /**
     * get url of key.
     * @param name key.
     * @return a url.
     */
    public String generateUrl(String name) {
        String objectKey = name;
        if (!obsClient.doesObjectExist(obsBucketName, objectKey)) {
            objectKey = "logo.png";
        }
        return "https://" + obsBucketName + "." + obsEndpoint + "/" + objectKey;
    }

    /**
     * list the keys of obs.
     */
    public void list() {
        ObjectListing obs = obsClient.listObjects(obsBucketName);
        for (ObsObject obo : obs.getObjects()) {
            LOGGER.info("key: {}", obo.getObjectKey());
        }
    }
}
