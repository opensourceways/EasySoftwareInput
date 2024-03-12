package com.software.service;

import com.obs.services.ObsClient;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;

import java.io.File;
import java.io.FilenameFilter;
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
            File directory = new File(Paths.get(subFolderPath, "doc", "picture").toString());
            if (directory.isDirectory()) {
                File[] imageFiles = directory.listFiles(new ImageFilenameFilter());
    
                if (imageFiles != null) {
                    for (File file : imageFiles) {
                        System.out.println(file.getName());
                        String key = subFolder.getName() + "." + file.getName().split("\\.")[1];
                        putData(key, file.getAbsolutePath());
                        logger.info("{} post to obs successfully.", key);
                    }
                }
            } else {
                logger.info("{} does not have logo.", subFolder.getName());
            }
        }
    }

    static class ImageFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".jpg") ||
                   name.toLowerCase().endsWith(".jpeg") ||
                   name.toLowerCase().endsWith(".png") ||
                   name.toLowerCase().endsWith(".gif") ||
                   name.toLowerCase().endsWith(".bmp");
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
