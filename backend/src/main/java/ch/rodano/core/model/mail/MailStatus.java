package ch.rodano.core.model.mail;

public enum MailStatus {
	PENDING("Pending"), SENT("Sent"), SIMULATED("Sent (simulated)"), CANCELED("Canceled"), FAILED("Failed");

	private final String status;

	MailStatus(final String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}
}
