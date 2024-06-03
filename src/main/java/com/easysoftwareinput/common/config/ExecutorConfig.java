package com.easysoftwareinput.common.config;

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.Data;

@Configuration
@EnableAsync
@Data
public class ExecutorConfig {
    /**
     * corePoolSize.
     */
    @Value("${async.executor.thread.core_pool_size}")
    private int corePoolSize;

    /**
     * maxPoolSize.
     */
    @Value("${async.executor.thread.max_pool_size}")
    private int maxPoolSize;

    /**
     * queueCapacity.
     */
    @Value("${async.executor.thread.queue_capacity}")
    private int queueCapacity;

    /**
     * namePrefix.
     */
    @Value("${async.executor.thread.name.prefix}")
    private String namePrefix;

    /**
     * create a threadpool.
     * @return a threadpool.
     */
    @Bean(name = "asyncServiceExecutor")
    public ThreadPoolTaskExecutor asyncServiceExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
        threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
        threadPoolTaskExecutor.setKeepAliveSeconds(3);
        threadPoolTaskExecutor.setThreadNamePrefix(namePrefix);
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }

    /**
     * create a threadpool.
     * @return a threadpool.
     */
    @Bean(name = "epkgasyncServiceExecutor")
    public ThreadPoolTaskExecutor epkgAsyncServiceExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
        threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
        threadPoolTaskExecutor.setKeepAliveSeconds(3);
        threadPoolTaskExecutor.setThreadNamePrefix(namePrefix);
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}
