package ch.rodano.mcp.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import ch.rodano.api.configuration.security.RobotBasicAuthenticationProvider;
import ch.rodano.core.model.robot.Robot;

@Profile({ "api" })
@Configuration
public class McpSecurityConfiguration {
	private final RobotBasicAuthenticationProvider robotBasicAuthenticationProvider;

	public McpSecurityConfiguration(final RobotBasicAuthenticationProvider robotBasicAuthenticationProvider) {
		this.robotBasicAuthenticationProvider = robotBasicAuthenticationProvider;
	}

	@Bean
	@Order(2)
	public SecurityFilterChain mcpFilterChain(final HttpSecurity http) {
		http
			.securityMatcher("/mcp", "/mcp/**")
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authenticationProvider(robotBasicAuthenticationProvider)
			.httpBasic(basic -> basic.realmName("Rodano MCP"))
			.authorizeHttpRequests(authorize -> authorize
				.anyRequest()
				.access((authentication, _) -> new AuthorizationDecision(authentication.get().getPrincipal() instanceof Robot))
			);
		return http.build();
	}
}
