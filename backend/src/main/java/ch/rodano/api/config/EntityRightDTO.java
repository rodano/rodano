package ch.rodano.api.config;

public record EntityRightDTO(
	boolean canRead,
	boolean canWrite
) {
}
