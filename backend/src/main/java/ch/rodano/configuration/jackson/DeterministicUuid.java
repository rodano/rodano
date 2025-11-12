package ch.rodano.configuration.jackson;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class DeterministicUuid {

	private DeterministicUuid() {
	}

	public static UUID deterministic(final UUID projectId, final String kind, final String code) {
		if(code == null) {
			return null;
		}
		return UUID.nameUUIDFromBytes((projectId + "|" + kind + "|" + code).getBytes(StandardCharsets.UTF_8));
	}
}
