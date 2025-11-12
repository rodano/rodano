package ch.rodano.core.services.project;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProjectIdResolver {

	private final String code;
	private final UUID id;

	public ProjectIdResolver(final @Value("${rodano.project.code}") String code) {
		if(code == null || code.isBlank()) {
			throw new IllegalStateException("rodano.project.code is missing/blank");
		}
		this.code = code.trim();
		this.id = UUID.nameUUIDFromBytes(("PROJECT:" + this.code).getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}

	public String code() {
		return code;
	}

	public UUID id() {
		return id;
	}
}
