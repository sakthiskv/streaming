package com.arka.streamingserv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactivefeign.spring.config.EnableReactiveFeignClients;

@SpringBootApplication
@EnableReactiveFeignClients
public class StreamingservApplication {

	public static void main(String[] args) {
		SpringApplication.run(StreamingservApplication.class, args);
	}

}
