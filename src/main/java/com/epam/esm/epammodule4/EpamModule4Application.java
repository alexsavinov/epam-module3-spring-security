package com.epam.esm.epammodule4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class EpamModule4Application extends SpringBootServletInitializer {

	public static void main(String[] args) {
		ConfigurableApplicationContext applicationContext = SpringApplication.run(EpamModule4Application.class, args);

		applicationContext.start();
	}
}
