package com.emr.medicare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.data.redis.autoconfigure.DataRedisRepositoriesAutoConfiguration;

@SpringBootApplication(exclude = { DataRedisRepositoriesAutoConfiguration.class })
public class MedicareApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedicareApplication.class, args);
	}

}
