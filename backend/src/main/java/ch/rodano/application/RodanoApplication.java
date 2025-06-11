package ch.rodano.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main class for the Spring application
 * This class must be kept in a dedicated package that has not descendant
 * The annotation @SpringBootApplication includes @ComponentScan so Spring will scan all the subpackages by default
 * For the application profiles "database" and "migration", we don't want to scan the API stuff
 *
 */
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class })
@EnableTransactionManagement
public class RodanoApplication {

	/**
	 * Main method to run the application
	 * Remember that in this code, Spring has not really started yet
	 *
	 * @param args The arguments given to run the application
	 */
	@SuppressWarnings("resource")
	public static void main(final String[] args) {
		new SpringApplication(RodanoApplication.class).run(args);
	}
}
