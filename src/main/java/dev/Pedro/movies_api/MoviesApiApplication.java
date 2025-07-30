package dev.Pedro.movies_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>
 * Main entry point for the Spring Boot MovieQuest Application.
 * </p>
 *
 * <p>
 * This class is responsible for bootstrapping the Spring application context
 * and
 * launching the web backend server.
 * </p>
 */
@SpringBootApplication
public class MoviesApiApplication {

	/**
	 * <p>
	 * Launches the Spring Boot application.
	 * </p>
	 *
	 * @param args [not used] command-line arguments passed to the application.
	 */
	public static void main(String[] args) {
		SpringApplication.run(MoviesApiApplication.class, args);
	}

}
