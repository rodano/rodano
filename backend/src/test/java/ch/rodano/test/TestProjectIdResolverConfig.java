package ch.rodano.test;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import ch.rodano.core.services.project.ProjectIdResolver;

@TestConfiguration
@Profile("test")
public class TestProjectIdResolverConfig {

	@Bean
	@Primary
	public ProjectIdResolver testProjectIdResolver(@Value("${rodano.test.project.code}") String projectCode,
												   @Value("${rodano.test.project.id}") String projectId) {
		return new TestProjectIdResolver(projectCode, UUID.fromString(projectId));
	}
}
