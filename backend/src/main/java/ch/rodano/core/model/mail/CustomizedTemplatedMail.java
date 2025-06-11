package ch.rodano.core.model.mail;

import java.util.Map;

public class CustomizedTemplatedMail extends AbstractTemplatedMail {
	private String subject;
	private String textBody;
	private String htmlBody;

	/**
	 * Constructor
	 *
	 * @param subject            The subject
	 * @param textBody           The text body
	 * @param htmlBody           The html body
	 * @param templateParameters The template parameters
	 */
	public CustomizedTemplatedMail(final String subject, final String textBody, final String htmlBody, final Map<String, Object> templateParameters) {
		super(templateParameters);
		this.subject = subject;
		this.textBody = textBody;
		this.htmlBody = htmlBody;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(final String subject) {
		this.subject = subject;
	}

	public String getTextBody() {
		return textBody;
	}

	public void setTextBody(final String textBody) {
		this.textBody = textBody;
	}

	public String getHtmlBody() {
		return htmlBody;
	}

	public void setHtmlBody(final String htmlBody) {
		this.htmlBody = htmlBody;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("CustomizedTemplatedMail{");
		sb.append(super.toString());
		sb.append(", subject='").append(subject).append('\'');
		sb.append(", textBody='").append(textBody).append('\'');
		sb.append(", htmlBody='").append(htmlBody).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
