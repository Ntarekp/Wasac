package rw.gov.wasac.ubsystem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class UbsystemApplicationTests {

	@Test
	void applicationClassLoads() {
		assertNotNull(UbsystemApplication.class);
	}

}
