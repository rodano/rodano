package ch.rodano.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

@Profile("test")
@Configuration
public class MailTestConfiguration {

	@Bean(destroyMethod = "stop")
	public GreenMail greenMail() {
		final var greenMail = new GreenMail(ServerSetupTest.SMTP);
		greenMail.start();
		return greenMail;
	}
}
