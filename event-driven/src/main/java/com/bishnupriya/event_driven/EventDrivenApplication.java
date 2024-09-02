package com.bishnupriya.event_driven;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class  EventDrivenApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventDrivenApplication.class, args);
	}

}
