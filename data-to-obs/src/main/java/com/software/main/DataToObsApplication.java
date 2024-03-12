package com.software.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.software.service.ObsService;

@SpringBootApplication
@ComponentScan(basePackages = { "com.software.*" })
public class DataToObsApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DataToObsApplication.class, args);

		ObsService obsService = (ObsService) context.getBean("obsService");
		obsService.task("openeuler-docker-images");
	}

}
