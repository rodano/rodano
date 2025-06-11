package ch.rodano.core.model.mail;

import java.util.Map;

public class DefinedTemplatedMail extends AbstractTemplatedMail {
	private MailTemplate template;

	public DefinedTemplatedMail(final MailTemplate template, final Map<String, Object> templateParameters) {
		super(templateParameters);
		this.template = template;
	}

	public MailTemplate getTemplate() {
		return template;
	}

	public void setTemplate(final MailTemplate template) {
		this.template = template;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("DefinedTemplatedMail{");
		sb.append(super.toString());
		sb.append(", template=").append(template);
		sb.append('}');
		return sb.toString();
	}
}
