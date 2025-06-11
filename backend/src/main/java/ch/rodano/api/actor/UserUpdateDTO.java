package ch.rodano.api.actor;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Update a user")
public record UserUpdateDTO(
	@Schema(description = "Name")
	String name,
	@Schema(description = "Is the user externally managed")
	boolean externallyManaged,
	@Schema(description = "Language ID")
	String languageId,
	@Schema(description = "Country ID")
	String countryId,
	@Schema(description = "Phone number")
	String phone
) {}
