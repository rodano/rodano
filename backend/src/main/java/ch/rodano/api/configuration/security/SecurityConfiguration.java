package ch.rodano.api.configuration.security;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;

import ch.rodano.api.configuration.filter.BearerTokenAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
	private final BasicAuthenticationEntryPoint basicAuthenticationEntryPoint;
	private final BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter;
	private final RobotBasicAuthenticationProvider robotBasicAuthenticationProvider;

	public SecurityConfiguration(
		final BasicAuthenticationEntryPoint basicAuthenticationEntryPoint,
		final BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter,
		final RobotBasicAuthenticationProvider robotBasicAuthenticationProvider
	) {
		super();
		this.basicAuthenticationEntryPoint = basicAuthenticationEntryPoint;
		this.bearerTokenAuthenticationFilter = bearerTokenAuthenticationFilter;
		this.robotBasicAuthenticationProvider = robotBasicAuthenticationProvider;
	}

	@Bean
	public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
		http
			//disable cross site request forging protection
			.csrf(
				AbstractHttpConfigurer::disable
			)
			//disable the default form login
			.formLogin(
				AbstractHttpConfigurer::disable
			)
			//disable the default logout function
			.logout(
				AbstractHttpConfigurer::disable
			)
			//disable the default session management
			.sessionManagement(
				sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			//set the default security context repository to the attribute request
			.securityContext(
				securityContext -> securityContext
					.securityContextRepository(new RequestAttributeSecurityContextRepository())
			)
			//configure the public and private endpoints
			.authorizeHttpRequests(
				authorize -> authorize
					.requestMatchers(HttpMethod.GET, "/config/public-study").permitAll()
					//administration and database
					.requestMatchers(HttpMethod.GET, "/administration/database/status", "/administration/maintenance", "/administration/debug", "/administration/is-online").permitAll()
					.requestMatchers(HttpMethod.POST, "/administration/database/bootstrap").permitAll()
					//sessions
					.requestMatchers(HttpMethod.POST, "/sessions").permitAll()
					.requestMatchers(HttpMethod.GET, "/sessions/delegated").permitAll()
					//user security tasks
					.requestMatchers(HttpMethod.POST, "/auth/password/recover", "/auth/password/reset").permitAll()
					.requestMatchers(HttpMethod.POST, "/epro/robot").permitAll()
					.requestMatchers(HttpMethod.GET, "/user/activation/**").permitAll()
					.requestMatchers(HttpMethod.POST, "/user/activation/**").permitAll()
					.requestMatchers(HttpMethod.POST, "/users/email-verification/{verificationCode:[\\w\\-]+}").permitAll()
					.requestMatchers(HttpMethod.POST, "/users/account-recovery/{recoveryCode:[\\w\\-]+}").permitAll()
					//resources and documentation
					.requestMatchers(HttpMethod.GET, "/resources/public", "/resources/public/{resourcePk:[0-9]+}/file").permitAll()
					.requestMatchers(HttpMethod.GET, "/api-docs", "/api-docs/**", "/api-docs.html", "/swagger-ui/**").permitAll()
					.requestMatchers("/actuator/**").hasAuthority(Authority.ROLE_ADMIN.name())
					.anyRequest().authenticated()
			)
			//reject requests that are not authenticated with a custom HTTP response code
			//by default Spring uses 403, but we want to return 401
			.exceptionHandling(
				exceptionHandling -> exceptionHandling.authenticationEntryPoint(unauthorizedEntryPoint())
			)
			//inject the robot authentication provider
			.authenticationProvider(robotBasicAuthenticationProvider)
			//this allows HTTP Basic authentication used by robots
			.httpBasic(
				httpBasic -> httpBasic.authenticationEntryPoint(basicAuthenticationEntryPoint)
			);

		//add the bearer token auth filter to the filter chain
		http.addFilterAfter(bearerTokenAuthenticationFilter, BasicAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * Create an unauthorized entry point bean
	 */
	@Bean
	public AuthenticationEntryPoint unauthorizedEntryPoint() {
		return (_, response, _) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}
}
