package ch.rodano.core.configuration.mail;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Configuration of the mail sender
 * The SMTP server is configured through the environment (spring.mail.* properties)
 * Not used in tests (see the MailTestConfiguration class that creates a placeholder SMTP server)
 */
@Configuration
@Profile("!test")
public class MailConfiguration {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final String smtpServer;
	private final Integer smtpPort;
	private final String smtpLogin;
	private final String smtpPassword;
	private final boolean smtpEnableTls;

	public MailConfiguration(
		@Value("${spring.mail.host:localhost}") final String smtpServer,
		@Value("${spring.mail.port:25}") final Integer smtpPort,
		@Value("${spring.mail.username:}") final String smtpLogin,
		@Value("${spring.mail.password:}") final String smtpPassword,
		@Value("${spring.mail.properties.mail.smtp.starttls.enable:true}") final boolean smtpEnableTls
	) {
		this.smtpServer = smtpServer;
		this.smtpPort = smtpPort;
		this.smtpLogin = smtpLogin;
		this.smtpPassword = smtpPassword;
		this.smtpEnableTls = smtpEnableTls;
	}

	@Bean
	public JavaMailSender getJavaMailSender() {
		final var sender = new JavaMailSenderImpl();

		sender.setHost(smtpServer);
		sender.setPort(smtpPort);

		if(StringUtils.isNotBlank(smtpLogin) && StringUtils.isNotBlank(smtpPassword)) {
			sender.setUsername(smtpLogin);
			sender.setPassword(smtpPassword);
		}

		final var properties = sender.getJavaMailProperties();
		properties.put("mail.transport.protocol", "smtp");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", smtpEnableTls);

		sender.setJavaMailProperties(properties);

		logger.info("Mail sender initialized with host {}, port {}, login {}", sender.getHost(), sender.getPort(), sender.getUsername());
		return sender;
	}
}
