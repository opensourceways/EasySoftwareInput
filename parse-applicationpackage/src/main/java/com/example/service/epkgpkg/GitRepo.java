package com.example.service.epkgpkg;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GitRepo {
    // 变量repoPath：`https://gitee.com/openeuler/openeuler-docker-images`仓库的本地目录
    @Value("${repo.path}")
    String repoPath;

    @Autowired
    ParseAppPkg parseAppPkg;

    public void run() {
        try {         
            Git git = Git.open(new File(repoPath));
            git.pull().call();
            git.close();
            dataToObs(repoPath);
        } catch (Exception e) {
            log.error("git pull exception", e);
        }
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
            log.info("{} does not exist or is not a directory.", folderPath);
        }
    }

    private void processSubFolder(File subFolder) {
        if (subFolder.isDirectory()) {
            String subFolderPath = subFolder.getAbsolutePath();
            String fullFile = Paths.get(subFolderPath, "doc", "image-info.yml").toString();
            File imageInfoYaml = new File(fullFile);
            if (! imageInfoYaml.exists() || ! imageInfoYaml.isFile()) {
                // log.info("{} does not have image-info.yaml", subFolder);
                return;
            }

            parseAppPkg.run(fullFile);
        }
    }
}
