package ch.rodano.core.constants;

import java.util.UUID;

public class SystemConstants {

	private SystemConstants() {
	}

	/**
	 * UUID for the SYSTEM project used for superuser administrative operations
	 */
	public static final UUID SYSTEM_PROJECT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

	/**
	 * Code for the SYSTEM project
	 */
	public static final String SYSTEM_PROJECT_CODE = "SYSTEM";
}
