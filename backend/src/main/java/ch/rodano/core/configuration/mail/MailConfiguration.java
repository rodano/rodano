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

@Configuration
@Profile("!test")
public class MailConfiguration {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final StudyService studyService;
	private final String envSmtpServer;
	private final Integer envSmtpPort;
	private final String envSmtpLogin;
	private final String envSmtpPassword;

	public MailConfiguration(
		final StudyService studyService,
		@Value("${spring.mail.host:localhost}") final String smtpServer,
		@Value("${spring.mail.port:25}") final Integer smtpPort,
		@Value("${spring.mail.username:}") final String smtpLogin,
		@Value("${spring.mail.password:}") final String smtpPassword
	) {
		this.studyService = studyService;
		this.envSmtpServer = smtpServer;
		this.envSmtpPort = smtpPort;
		this.envSmtpLogin = smtpLogin;
		this.envSmtpPassword = smtpPassword;
	}

	@Bean
	public JavaMailSender getJavaMailSender() {
		final var sender = new JavaMailSenderImpl();

		final var study = studyService.getStudy();

		sender.setHost(StringUtils.defaultIfBlank(study.getSmtpServer(), envSmtpServer));
		sender.setPort(Optional.ofNullable(study.getSmtpPort()).orElse(envSmtpPort));

		final var login = StringUtils.defaultIfBlank(study.getSmtpLogin(), envSmtpLogin);
		if(StringUtils.isNotBlank(login)) {
			sender.setUsername(login);
		}

		final var password = StringUtils.defaultIfBlank(study.getSmtpPassword(), envSmtpPassword);
		if(StringUtils.isNotBlank(password)) {
			sender.setPassword(password);
		}

		final var properties = sender.getJavaMailProperties();
		properties.put("mail.transport.protocol", "smtp");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", study.getSmtpTLS());

		sender.setJavaMailProperties(properties);

		logger.info("Mail sender initialized with host {}, port {}, login {}", sender.getHost(), sender.getPort(), sender.getUsername());
		return sender;
	}
}
