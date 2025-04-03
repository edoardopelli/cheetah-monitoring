package org.cheetah.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CheetahMonitoringApplication {

	public static void main(String[] args) {
		SpringApplication.run(CheetahMonitoringApplication.class, args);
	}

}
