package ch.rodano.test;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;

import ch.rodano.core.services.project.ProjectIdResolver;

public class TestProjectIdResolver extends ProjectIdResolver {

	private UUID testProjectId;
	private final String testProjectCode;

	public TestProjectIdResolver(@Value("${rodano.test.project.code}") final String testProjectCode,
								 @Value("${rodano.test.project.id}") final UUID testProjectId) {
		super(testProjectCode, null);
		this.testProjectCode = testProjectCode;
		this.testProjectId = testProjectId;
	}

	@Override
	public String code() {
		return testProjectCode;
	}

	@Override
	public UUID id() {
		return testProjectId;
	}

	@Override
	public void setProjectId(final UUID projectId) {
		this.testProjectId = projectId;
	}

	@Override
	public UUID resolveCode(final String code) {
		if(testProjectCode.equals(code)) {
			return testProjectId;
		}
		return null;
	}

	@Override
	public boolean hasProject() {
		return testProjectId != null;
	}

	@Override
	public void clearProject() {
	}
}
