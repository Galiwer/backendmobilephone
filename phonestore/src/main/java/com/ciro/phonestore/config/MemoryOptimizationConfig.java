package com.ciro.phonestore.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@EnableAsync
public class MemoryOptimizationConfig {

    @Bean
    @Profile("prod")
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                connector.setMaxParameterCount(500);
                connector.setMaxPostSize(1024 * 1024); // 1MB

                if (connector instanceof org.apache.catalina.connector.Connector) {
                    ((org.apache.catalina.connector.Connector) connector).setMaxSavePostSize(1024 * 1024); // 1MB
                }
            });
        };
    }

    @Bean
    @Profile("prod")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("AsyncThread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    @Profile("prod")
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(50)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .weakKeys()
                .softValues());
        return cacheManager;
    }
}