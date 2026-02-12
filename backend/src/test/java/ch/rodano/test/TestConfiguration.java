package ch.rodano.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.RestTestClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;

import tools.jackson.databind.json.JsonMapper;

@Profile("test")
@Configuration
@ComponentScan(basePackages = { "ch.rodano" })
public class TestConfiguration {

	@Bean
	public RestTestClientBuilderCustomizer restTestClientBuilderCustomizer(@Autowired final JsonMapper mapper) {
		return builder -> {
			builder.defaultHeader(HttpHeaders.USER_AGENT, "Integration Test");
			builder.configureMessageConverters(client -> {
				client.registerDefaults().withJsonConverter(new JacksonJsonHttpMessageConverter(mapper));
			});
		};
	}
}
