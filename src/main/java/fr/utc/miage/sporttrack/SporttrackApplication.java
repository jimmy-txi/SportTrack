package fr.utc.miage.sporttrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the SportTrack Spring Boot application.
 *
 * <p>Enables auto-configuration, component scanning, and scheduled task
 * execution via {@code @EnableScheduling}. This class bootstraps the entire
 * application context, including web MVC, security, data access, and
 * service layers.</p>
 *
 * @author SportTrack Team
 */
@EnableScheduling
@SpringBootApplication
public class SporttrackApplication {

	/**
	 * Launches the SportTrack application.
	 *
	 * @param args command-line arguments passed at startup, if any
	 */
	public static void main(String[] args) {
		SpringApplication.run(SporttrackApplication.class, args);
	}

}
