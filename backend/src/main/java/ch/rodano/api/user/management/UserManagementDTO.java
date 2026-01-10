package ch.rodano.api.user.management;

import java.time.ZonedDateTime;

public record UserManagementDTO(
	Long pk,
	String name,
	String email,
	boolean isSuperuser,
	boolean isActivated,
	boolean isDeleted,
	boolean isExternallyManaged,
	boolean blocked,
	boolean hasPassword,
	ZonedDateTime creationTime,
	ZonedDateTime lastUpdateTime,
	ZonedDateTime loginDate,
	String languageId,
	String phone
) {
}
