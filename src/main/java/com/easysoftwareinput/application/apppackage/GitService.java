package com.easysoftwareinput.application.apppackage;


import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.common.utils.FileUtil;
import com.easysoftwareinput.domain.git.GitConfig;

@Component
public class GitService {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GitService.class);

    /**
     * config.
     */
    @Autowired
    private GitConfig config;


    /**
     * clone or pull the repo.
     */
    public void cloneOrPull() {
        File repo = new File(config.getLocalPath());
        FileUtil.mkdirIfUnexist(repo);

        File[] files = repo.listFiles((dir, name) -> ".git".equals(name));
        if (files == null) {
            return;
        }
        if (files.length == 0) {
            cloneRepo();
        } else {
            pullRepo();
        }
    }

    /**
     * get provider.
     * @return provider.
     */
    public UsernamePasswordCredentialsProvider getProvider() {
        return new UsernamePasswordCredentialsProvider(config.getUserName(), config.getPassword());
    }

    /**
     * git pull the repo.
     */
    public void pullRepo() {
        UsernamePasswordCredentialsProvider provider = getProvider();
        try {
            Git git = Git.open(new File(config.getLocalPath()));
            git.pull()
                    .setRemoteBranchName("master")
                    .setCredentialsProvider(provider)
                    .call();
        } catch (Exception e) {
            LOGGER.error("fail to git pull repo: {}, err: {}", config.getLocalPath(), e.getMessage());
        }
    }

    /**
     * clone the repo.
     */
    public void cloneRepo() {
        UsernamePasswordCredentialsProvider provider = getProvider();
        try (Git git = Git.cloneRepository()
                .setURI(config.getRemotePath())
                .setDirectory(new File(config.getLocalPath()))
                .setCredentialsProvider(provider)
                .setCloneSubmodules(true)
                .setBranch(config.getBranch())
                .call()) {
            git.getRepository().close();
            git.close();
        } catch (GitAPIException e) {
            LOGGER.error("fail to clone repo: {}", config.getLocalPath());
        }
    }

}
