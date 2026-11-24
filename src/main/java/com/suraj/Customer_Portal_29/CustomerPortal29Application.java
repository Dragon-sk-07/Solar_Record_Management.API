package com.suraj.Customer_Portal_29;

import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
@EnableScheduling
@EnableCaching
@SpringBootApplication
public class CustomerPortal29Application {

	public static void main(String[] args) {
		SpringApplication.run(CustomerPortal29Application.class, args);
	}

}
