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
		SpringApplication.run(PhonestoreApplication.class, args);
	}

	@Override
	public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
		String[] profiles = env.getActiveProfiles();
		String activeProfile = profiles.length > 0 ? profiles[0] : "default";

		logger.info("Application context refreshed");
		logger.info("Active profile: {}", activeProfile);
		logger.info("Database URL: {}", env.getProperty("spring.datasource.url"));
		logger.info("Server port: {}", env.getProperty("server.port"));
	}
}
