package ch.rodano.api.project;

import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ProjectDTO(
	@NotNull UUID projectId,
	@NotNull String code,
	Map<String, String> shortname,
	Map<String, String> longname,
	Map<String, String> description,
	String url,
	String color,
	String introductionText
) { }
