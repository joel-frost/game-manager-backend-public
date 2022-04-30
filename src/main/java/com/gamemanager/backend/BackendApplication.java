package com.gamemanager.backend;

import com.gamemanager.backend.appUser.AppUser;
import com.gamemanager.backend.appUser.AppUserService;
import com.gamemanager.backend.appUser.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	CommandLineRunner run(AppUserService appUserService) {
		return args -> {
			appUserService.saveRole(new Role(null, "ROLE_USER"));
			appUserService.saveRole(new Role(null, "ROLE_ADMIN"));

			appUserService.saveAppUser(new AppUser(null, "Joel", "Frost", "joelfrost4@gmail.com", "password123", new ArrayList<>(), new ArrayList<>()));
			appUserService.saveAppUser(new AppUser(null, "John", "Smith", "john@gmail.com", "password123", new ArrayList<>(), new ArrayList<>()));
			appUserService.saveAppUser(new AppUser(null, "Lauren", "Georgia", "lauren@gmail.com", "password123", new ArrayList<>(), new ArrayList<>()));
			appUserService.saveAppUser(new AppUser(null, "Pete", "Greggs", "pete@gmail.com", "password123", new ArrayList<>(), new ArrayList<>()));

			appUserService.addRoleToAppUser("joelfrost4@gmail.com", "ROLE_USER");
			appUserService.addRoleToAppUser("joelfrost4@gmail.com", "ROLE_ADMIN");
			appUserService.addRoleToAppUser("john@gmail.com", "ROLE_USER");
			appUserService.addRoleToAppUser("lauren@gmail.com", "ROLE_USER");

		};
	}
}

