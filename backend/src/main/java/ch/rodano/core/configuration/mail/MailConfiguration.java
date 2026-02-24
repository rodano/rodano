package ch.rodano.core.configuration.mail;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import ch.rodano.core.services.bll.study.StudyService;

/**
 * Configuration of the mail sender
 * The sender can be configured using the study configuration, or the environment configuration
 * Study SMTP configuration always override environment configuration
 * Not used in tests (see the MailTestConfiguration class that creates a placeholder SMTP server)
 * In tests, there is no need to configure the SMTP server based on the configuration properties
 */
@Configuration
@Profile("!test")
public class MailConfiguration {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final StudyService studyService;
	private final String envSmtpServer;
	private final Integer envSmtpPort;
	private final String envSmtpLogin;
	private final String envSmtpPassword;
	private final boolean envSmtpEnableTls;

	public MailConfiguration(
		final StudyService studyService,
		@Value("${spring.mail.host:localhost}") final String smtpServer,
		@Value("${spring.mail.port:25}") final Integer smtpPort,
		@Value("${spring.mail.username:}") final String smtpLogin,
		@Value("${spring.mail.password:}") final String smtpPassword,
		@Value("${spring.mail.properties.mail.smtp.starttls.enable:true}") final boolean smtpEnableTls
	) {
		this.studyService = studyService;
		this.envSmtpServer = smtpServer;
		this.envSmtpPort = smtpPort;
		this.envSmtpLogin = smtpLogin;
		this.envSmtpPassword = smtpPassword;
		this.envSmtpEnableTls = smtpEnableTls;
	}

	@Bean
	public JavaMailSender getJavaMailSender() {
		final var sender = new JavaMailSenderImpl();

		final var study = studyService.getStudy();

		sender.setHost(StringUtils.defaultIfBlank(study.getSmtpServer(), envSmtpServer));
		sender.setPort(Optional.ofNullable(study.getSmtpPort()).orElse(envSmtpPort));

		final var login = StringUtils.defaultIfBlank(study.getSmtpLogin(), envSmtpLogin);
		final var password = StringUtils.defaultIfBlank(study.getSmtpPassword(), envSmtpPassword);

		if(StringUtils.isNotBlank(login) && StringUtils.isNotBlank(password)) {
			sender.setUsername(login);
			sender.setPassword(password);
		}

		final var properties = sender.getJavaMailProperties();
		properties.put("mail.transport.protocol", "smtp");
		properties.put("mail.smtp.auth", "true");

		//there is no way to tell if SMTP TLS is disabled or not configured in the study configuration
		//that's because configuration only handle boolean, not Boolean, hence cannot have a null value
		//we can guess that if a server has been set in the study configuration, the TLS flag from the study configuration must be used
		//otherwise, use the environment configuration
		properties.put("mail.smtp.starttls.enable", StringUtils.isNotBlank(study.getSmtpServer()) ? study.getSmtpTLS() : envSmtpEnableTls);

		sender.setJavaMailProperties(properties);

		logger.info("Mail sender initialized with host {}, port {}, login {}", sender.getHost(), sender.getPort(), sender.getUsername());
		return sender;
	}
}
