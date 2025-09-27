package com.razorquake.razorlinks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RazorlinksApplication {

	public static void main(String[] args) {
		SpringApplication.run(RazorlinksApplication.class, args);
	}

}
