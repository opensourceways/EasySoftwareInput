package com.easysoftwareinput.application.apppackage;

import com.obs.services.ObsClient;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AppHandler {

    @Value("${obs.endpoint}")
    String obsEndpoint;

    @Value("${obs.bucket}")
    String obsBucketName;

    @Value("${obs.ak}")
    String obsAk;

    @Value("${obs.sk}")
    String obsSk;

    @Autowired
    YamlService yamlService;

    private static final Logger logger = LoggerFactory.getLogger(AppHandler.class);
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

    public String generateUrl(String name) {
        String objectKey = name + ".png";
        if(!obsClient.doesObjectExist(obsBucketName, objectKey)) {
            objectKey = "logo.png";
        }
        return "https://" + obsBucketName + "." + obsEndpoint + "/" + objectKey;
    }

    public void parseEachApp(String folderPath) {
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files == null)
                return;

            for (File file : files) {
                String picName = parseImageInfo(file);
                parsePicture(file, picName);
                
            }
        } else {
            logger.info("{} does not exist or is not a directory.", folderPath);
        }
    }

    private void parsePicture(File subFolder, String picName) {
        if (subFolder.isDirectory()) {
            String subFolderPath = subFolder.getAbsolutePath();
            File directory = new File(Paths.get(subFolderPath, "doc", "picture").toString());
            if (directory.isDirectory()) {
                File[] imageFiles = directory.listFiles(new ImageFilenameFilter());
    
                if (imageFiles != null) {
                    for (File file : imageFiles) {
                        String key = "";
                        if (StringUtils.isBlank(picName)) {
                            key = subFolder.getName() + ".png";
                        } else {
                            key = picName + ".png";
                        }

                        putData(key, file.getAbsolutePath());
                        logger.info("{} post to obs successfully.", key);
                    }
                }
            } else {
                logger.info("{} does not have logo.", subFolder.getName());
            }
        }
    }

    private String parseImageInfo(File subFolder) {
        if (subFolder.isDirectory()) {
            String subFolderPath = subFolder.getAbsolutePath();
            String fullFile = Paths.get(subFolderPath, "doc", "image-info.yml").toString();
            File imageInfoYaml = new File(fullFile);
            if (! imageInfoYaml.exists() || ! imageInfoYaml.isFile()) {
                logger.info("{} does not have image-info.yaml", subFolder);
                return "";
            }
            return yamlService.run(fullFile);
        }
        return "";
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
            parseEachApp(repoPath);
        } catch (Exception e) {
            logger.error("git pull exception", e);
        }
    }
}
