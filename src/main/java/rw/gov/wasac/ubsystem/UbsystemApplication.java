package rw.gov.wasac.ubsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UbsystemApplication {
	public static void main(String[] args) {
		SpringApplication.run(UbsystemApplication.class, args);
	}
}