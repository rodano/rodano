package ch.rodano.core.model.mail;

public enum MailOrigin {
	UNDEFINED("Undefined"),
	RULE("Rule"),
	TASK("Task"),
	USER("User"),
	SYSTEM("System"),
	ERROR("Error");

	private final String origin;

	MailOrigin(final String origin) {
		this.origin = origin;
	}

	public String getOrigin() {
		return origin;
	}
}
