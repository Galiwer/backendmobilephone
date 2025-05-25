package com.ciro.phonestore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.core.env.Environment;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

@SpringBootApplication
@ComponentScan(basePackages = { "com.ciro.phonestore" })
@EntityScan(basePackages = { "com.ciro.phonestore.models" })
@EnableJpaRepositories(basePackages = { "com.ciro.phonestore.repository" })
public class PhonestoreApplication implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger logger = LoggerFactory.getLogger(PhonestoreApplication.class);

	@Autowired
	private Environment env;

	public static void main(String[] args) {
		try {
			ApplicationContext context = SpringApplication.run(PhonestoreApplication.class, args);
			logger.info("Application started successfully");

			// Verify critical beans are loaded
			String[] beanNames = context.getBeanDefinitionNames();
			logger.debug("Total beans loaded: {}", beanNames.length);

			// Log active profiles
			Environment env = context.getEnvironment();
			String[] activeProfiles = env.getActiveProfiles();
			logger.info("Active profiles: {}", String.join(", ", activeProfiles));

		} catch (Exception e) {
			logger.error("Application failed to start: {}", e.getMessage(), e);
			System.exit(1);
		}
	}

	@Override
	public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
		try {
			String[] profiles = env.getActiveProfiles();
			String activeProfile = profiles.length > 0 ? profiles[0] : "default";

			logger.info("Application context refreshed");
			logger.info("Active profile: {}", activeProfile);

			// Log database configuration
			String dbUrl = env.getProperty("spring.datasource.url");
			logger.info("Database URL: {}", dbUrl != null ? dbUrl : "not configured");

			// Log server configuration
			String serverPort = env.getProperty("server.port", "8080");
			logger.info("Server port: {}", serverPort);

			// Log application configuration
			logger.info("Application name: {}", env.getProperty("spring.application.name"));
			logger.info("Maximum file size: {}", env.getProperty("spring.servlet.multipart.max-file-size"));
			logger.info("Upload directory: {}", env.getProperty("app.upload.dir"));
			logger.info("JPA show SQL: {}", env.getProperty("spring.jpa.show-sql"));
			logger.info("Hibernate DDL auto: {}", env.getProperty("spring.jpa.hibernate.ddl-auto"));

			// Verify critical properties
			if (dbUrl == null || dbUrl.trim().isEmpty()) {
				logger.warn("Database URL is not configured properly");
			}

			String uploadDir = env.getProperty("app.upload.dir");
			if (uploadDir == null || uploadDir.trim().isEmpty()) {
				logger.warn("Upload directory is not configured properly");
			}

		} catch (Exception e) {
			logger.error("Error during application context refresh: {}", e.getMessage(), e);
			throw e;
		}
	}
}
