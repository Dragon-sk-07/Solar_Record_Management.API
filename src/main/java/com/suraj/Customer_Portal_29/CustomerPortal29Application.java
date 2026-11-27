package com.suraj.Customer_Portal_29;

import com.suraj.Customer_Portal_29.repository.OwnerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

@EnableScheduling
@EnableCaching
@SpringBootApplication
public class CustomerPortal29Application {

	public static void main(String[] args) {
		SpringApplication.run(CustomerPortal29Application.class, args);
	}

	@Bean
	public CommandLineRunner migrateUsernames(OwnerRepository ownerRepository) {
		return args -> {
			var owners = ownerRepository.findAll();
			boolean needsUpdate = false;

			for (var owner : owners) {
				if (owner.getUsername() == null || owner.getUsername().isEmpty()) {
					owner.setUsername(owner.getEmail());
					needsUpdate = true;
				}
			}

			if (needsUpdate) {
				ownerRepository.saveAll(owners);
				System.out.println("Migration Complete: Populated 'username' for existing users with their emails.");
			}
		};
	}
}