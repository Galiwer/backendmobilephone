package com.ciro.phonestore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

@SpringBootApplication
public class PhonestoreApplication implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger logger = LoggerFactory.getLogger(PhonestoreApplication.class);

	@Autowired
	private Environment env;

	public static void main(String[] args) {
		try {
			logger.info("Starting Phonestore Application...");
			SpringApplication app = new SpringApplication(PhonestoreApplication.class);
			Environment env = app.run(args).getEnvironment();
			logger.info("Application started successfully");
			logger.info("Running with profile: {}", env.getActiveProfiles()[0]);
		} catch (Exception e) {
			logger.error("Error starting application", e);
			throw e;
		}
	}

	@Override
	public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
		try {
			logger.info("Application context refreshed");
			logger.info("Active profile: {}", env.getActiveProfiles()[0]);
			logger.info("Database URL: {}", env.getProperty("spring.datasource.url"));
			logger.info("Server port: {}", env.getProperty("server.port"));
		} catch (Exception e) {
			logger.error("Error during context refresh", e);
			throw e;
		}
	}
}
