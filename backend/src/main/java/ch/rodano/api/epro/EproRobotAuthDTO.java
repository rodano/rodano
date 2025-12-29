package ch.rodano.api.epro;

import java.util.UUID;

public record EproRobotAuthDTO(
	Long scopePk,
	String robotName,
	String robotKey,
	UUID projectId
) {}
