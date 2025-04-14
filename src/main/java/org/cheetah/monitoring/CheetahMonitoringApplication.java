package org.cheetah.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CheetahMonitoringApplication {

	public static void main(String[] args) throws Exception {
		if(System.getenv("MONGODB_URI")==null) {
			throw new Exception("Environment MONGODB_URI is missing");
		}
		if(System.getenv("TELEGRAM_BOT_TOKEN")==null) {
			throw new Exception("Environment TELEGRAM_BOT_TOKEN is missing");
		}
		if(System.getenv("TELEGRAM_CHAT_ID")==null) {
			throw new Exception("Environment TELEGRAM_CHAT_ID is missing");
		}
		SpringApplication.run(CheetahMonitoringApplication.class, args);
	}

}
