package com.helioauth.passkeys.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PasskeysApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PasskeysApiApplication.class, args);
	}

}
