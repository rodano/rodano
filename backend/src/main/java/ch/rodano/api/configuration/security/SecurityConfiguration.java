package ch.rodano.api.configuration.security;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;

import ch.rodano.core.model.session.Session;
import ch.rodano.core.model.user.User;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
	private final BasicAuthenticationEntryPoint basicAuthenticationEntryPoint;
	private final RobotBasicAuthenticationProvider robotBasicAuthenticationProvider;
	private final SessionOpaqueTokenIntrospector sessionOpaqueTokenIntrospector;

	public SecurityConfiguration(
		final BasicAuthenticationEntryPoint basicAuthenticationEntryPoint,
		final RobotBasicAuthenticationProvider robotBasicAuthenticationProvider,
		final SessionOpaqueTokenIntrospector sessionOpaqueTokenIntrospector
	) {
		super();
		this.basicAuthenticationEntryPoint = basicAuthenticationEntryPoint;
		this.robotBasicAuthenticationProvider = robotBasicAuthenticationProvider;
		this.sessionOpaqueTokenIntrospector = sessionOpaqueTokenIntrospector;
	}

	//the goal of having 2 different security chain is to completely disabled authentication management for this endpoint
	//bearer tokens are not even tried to be parsed
	@Bean
	@Order(1)
	public SecurityFilterChain publicFilterChain(final HttpSecurity http) {
		http
			.securityMatchers(
				matchers -> matchers
					//config
					.requestMatchers(HttpMethod.GET, "/config/public-study")
					//administration and database
					.requestMatchers(HttpMethod.GET, "/administration/database/status", "/administration/maintenance", "/administration/debug", "/administration/is-online")
					.requestMatchers(HttpMethod.POST, "/administration/database/bootstrap")
					//sessions
					.requestMatchers(HttpMethod.POST, "/sessions")
					.requestMatchers(HttpMethod.GET, "/sessions/delegated")
					//user security tasks
					.requestMatchers(HttpMethod.POST, "/auth/password/recover", "/auth/password/reset")
					.requestMatchers(HttpMethod.POST, "/epro/robot")
					.requestMatchers(HttpMethod.GET, "/user/activation/**")
					.requestMatchers(HttpMethod.POST, "/user/activation/**")
					.requestMatchers(HttpMethod.POST, "/users/email-verification/**", "/users/account-recovery/**")
					//resources and documentation
					.requestMatchers(HttpMethod.GET, "/resources/public", "/resources/public/*/file")
					.requestMatchers(HttpMethod.GET, "/api-docs", "/api-docs/**", "/api-docs.html", "/swagger-ui/**")
			)
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(
				sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			.authorizeHttpRequests(
				authorize -> authorize.anyRequest().permitAll()
			);
		return http.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain privateFilterChain(final HttpSecurity http) {
		http
			//disable cross site request forging protection
			.csrf(AbstractHttpConfigurer::disable)
			//disable the default form login
			.formLogin(AbstractHttpConfigurer::disable)
			//disable the default logout function
			.logout(AbstractHttpConfigurer::disable)
			//disable the default session management
			.sessionManagement(
				sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			//set the default security context repository to the attribute request
			.securityContext(
				securityContext -> securityContext
					.securityContextRepository(new RequestAttributeSecurityContextRepository())
			)
			.authorizeHttpRequests(
				authorize -> authorize
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
			)
			//bearer token authentication via opaque token introspection against the session store
			.oauth2ResourceServer(
				oauth2 -> oauth2.opaqueToken(
					opaque -> opaque
						.introspector(sessionOpaqueTokenIntrospector)
						.authenticationConverter((_, principal) -> {
							final var user = (User) principal.getAttribute("user");
							final var session = (Session) principal.getAttribute("session");
							return new UsernamePasswordAuthenticationToken(user, session, principal.getAuthorities());
						})
				)
			);
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
