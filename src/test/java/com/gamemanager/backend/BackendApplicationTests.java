package com.gamemanager.backend;

import com.gamemanager.backend.appUser.AppUser;
import com.gamemanager.backend.appUser.AppUserController;
import com.gamemanager.backend.appUserGame.AppUserGame;
import com.gamemanager.backend.appUserGame.AppUserGameController;
import com.gamemanager.backend.game.Game;
import com.gamemanager.backend.game.GameController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BackendApplicationTests {

	@Autowired
	private GameController gameController;
	@Autowired
	private AppUserController appUserController;
	@Autowired
	private AppUserGameController appUserGameController;

	private final Principal principal = new Principal() {
		@Override
		public String getName() {
			return "john@gmail.com";
		}
	};

	@SuppressWarnings("EmptyMethod")
	@Test
	void contextLoads() {
	}

	@Test
	void getGames() {
		List<Game> response = gameController.getAllGames();
		assertInstanceOf(List.class, response);
	}

	@Test
	void getAppUsers() {
		List<AppUser> response = appUserController.getUsers().getBody();
		assertEquals(response.size(), 4);
	}

	@Test
	void findByEmail() {
		AppUser response = appUserController.findByEmail("john@gmail.com", principal).getBody();
		assertEquals(response.getFirstName(), "John");
	}

	@Test
	void updateAppUser() {
		AppUser appUser = appUserController.findByEmail("john@gmail.com", principal).getBody();
		appUser.setLastName("Jones");
		appUserController.updateAppUser("john@gmail.com", appUser, principal);
		AppUser response = appUserController.findByEmail("john@gmail.com", principal).getBody();
		assertEquals(response.getLastName(), "Jones");
	}


	@Test
	void createUser() {
		AppUser appUser = new AppUser(null, "Test", "Person", "test@gmail.com", "password", null, null);
		appUserController.createUser(appUser);
		AppUser response = appUserController.findByEmail("test@gmail.com", new Principal() {
			@Override
			public String getName() {
				return "test@gmail.com";
			}
		}).getBody();
		assertEquals(response.getFirstName(), "Test");
	}

	@Test
	void addGame() {
		AppUser appUser = appUserController.findByEmail("john@gmail.com", principal).getBody();
		Game game = new Game(null,"Test Game", "Test Description", -1, -1f, null, "test genre", -1);
		appUserGameController.addGame(appUser.getId(), game);
		Collection<AppUserGame> response = appUserController.getGames("john@gmail.com", principal).getBody();
		assertEquals(response.size(), 1);
	}

}
