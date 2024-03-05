package com.software.service;

import com.obs.services.ObsClient;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ObsService {

    @Value("${obs.endpoint}")
    String obsEndpoint;

    @Value("${obs.bucket}")
    String obsBucketName;

    @Value("${obs.ak}")
    String obsAk;

    @Value("${obs.sk}")
    String obsSk;

    private static final Logger logger = LoggerFactory.getLogger(ObsService.class);
    private static ObsClient obsClient;

    @PostConstruct
    public void init() {
        obsClient =  new ObsClient(obsAk, obsSk, obsEndpoint);
    }

    public void putData(String ObjectKey, String filePath) {
        PutObjectRequest request = new PutObjectRequest();
        request.setBucketName(obsBucketName);
        request.setObjectKey(ObjectKey);
        request.setFile(new File(filePath));
        obsClient.putObject(request);
    }

    public InputStream getData(String ObjectKey) {
        ObsObject object = obsClient.getObject(obsBucketName, ObjectKey);
        InputStream res = object.getObjectContent();
        return res;
    }

    public String generateUrl(String objectKey) {
        String publicUrl = "https://" + obsBucketName + "." + obsEndpoint + "/" + objectKey;
        return publicUrl;
    }

    public void dataToObs(String folderPath) {
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files == null)
                return;

            for (File file : files) {
                processSubFolder(file);
            }
        } else {
            logger.info("{} does not exist or is not a directory.", folderPath);
        }
    }

    private void processSubFolder(File subFolder) {
        if (subFolder.isDirectory()) {
            String subFolderPath = subFolder.getAbsolutePath();
            String logoPath = Paths.get(subFolderPath, "doc", "picture", "logo.png").toString();
            File logoFile = new File(logoPath);
            if (logoFile.exists()) {
                putData(subFolder.getName() + ".png", logoPath);
            }
        }
    }

    public void task(String repoPath) {
        try {         
            Git git = Git.open(new File(repoPath));
            git.pull().call();
            git.close();
            dataToObs(repoPath);
        } catch (Exception e) {
            logger.error("git pull exception", e);
        }
    }

}
