package ch.rodano.api.user.management;

public record ToggleSuperuserRequest(
	boolean isSuperuser
) {
}
