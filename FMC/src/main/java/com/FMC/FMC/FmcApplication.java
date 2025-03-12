package com.FMC.FMC;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FmcApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.directory("./")
				.ignoreIfMissing()
				.load();
		System.setProperty("OPENROUTESERVICE_API_KEY", dotenv.get("OPENROUTESERVICE_API_KEY"));

		SpringApplication.run(FmcApplication.class, args);
	}

}
