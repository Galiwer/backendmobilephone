package com.ciro.phonestore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.core.env.Environment;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

@SpringBootApplication
@ComponentScan(basePackages = "com.ciro.phonestore")
@EntityScan("com.ciro.phonestore.models")
@EnableJpaRepositories("com.ciro.phonestore.repository")
public class PhonestoreApplication implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger logger = LoggerFactory.getLogger(PhonestoreApplication.class);

	@Autowired
	private Environment env;

	public static void main(String[] args) {
		try {
			SpringApplication app = new SpringApplication(PhonestoreApplication.class);
			app.run(args);
			logger.info("Application started successfully");
		} catch (Exception e) {
			logger.error("Application failed to start", e);
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
			logger.info("Database URL: {}", env.getProperty("spring.datasource.url"));
			logger.info("Server port: {}", env.getProperty("server.port"));

			// Log important configuration settings
			logger.info("Maximum file size: {}", env.getProperty("spring.servlet.multipart.max-file-size"));
			logger.info("Upload directory: {}", env.getProperty("upload.path"));
			logger.info("JPA show SQL: {}", env.getProperty("spring.jpa.show-sql"));
			logger.info("Hibernate DDL auto: {}", env.getProperty("spring.jpa.hibernate.ddl-auto"));
		} catch (Exception e) {
			logger.error("Error during application context refresh", e);
			throw e;
		}
	}
}
