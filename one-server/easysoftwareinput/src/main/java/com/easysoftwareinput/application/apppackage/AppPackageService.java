package com.easysoftwareinput.application.apppackage;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
public class AppPackageService {
    private static final Logger logger = LoggerFactory.getLogger(AppPackageService.class);

    @Value("${repo.path}")
    String repoPath;

    @Autowired
    AppHandler appHandler;

    public void gitPull(String repoPath) {
        try {         
            Git git = Git.open(new File(repoPath));
            git.pull().call();
            git.close();
        } catch (Exception e) {
            logger.error("git pull exception", e);
        }
    }

    public void run() {
        // gitPull(repoPath);
        appHandler.parseEachApp(repoPath);
    }
}
