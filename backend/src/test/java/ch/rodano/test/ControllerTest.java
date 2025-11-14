package ch.rodano.test;

import java.io.IOException;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.web.servlet.client.RestTestClient;

import ch.rodano.api.authentication.AuthenticationDTO;
import ch.rodano.api.authentication.CredentialsDTO;
import ch.rodano.core.database.initializer.DatabaseInitializer;

import tools.jackson.databind.json.JsonMapper;

public class ControllerTest extends DatabaseTest {

	protected final String adminOnStudyEmail = "test+test-admin@rodano.ch";
	protected final String investigatorOnStudyEmail = "test+test-investigator@rodano.ch";
	protected final String dataManagerOnStudyEmail = "test+test-datamanager@rodano.ch";

	@Inject
	protected JsonMapper mapper;

	@Value("${rodano.pagination.maximum-page-size}")
	protected Integer MAX_PAGE_SIZE;

	@Value("${server.servlet.context-path}")
	protected String contextPath;

	@Autowired
	protected RestTestClient client;

	protected Optional<String> token = Optional.empty();

	@BeforeEach
	protected void setupClient() {
		client = client.mutate()
			.requestInterceptor(new ClientHttpRequestInterceptor() {
				@Override
				public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {
					if(token.isPresent()) {
						final HttpHeaders headers = request.getHeaders();
						if(!headers.containsHeader(HttpHeaders.AUTHORIZATION)) {
							headers.setBearerAuth(token.get());
						}
					}
					return execution.execute(request, body);
				}

			})
			.build();
	}

	/**
	 * Clear the client authentication
	 */
	@AfterEach
	protected void clearAuthentication() {
		token = Optional.empty();
	}

	/**
	 * Authenticate with the default password
	 */
	protected void authenticate(final String email) {
		final var credentials = new CredentialsDTO();
		credentials.setEmail(email);
		credentials.setPassword(DatabaseInitializer.DEFAULT_PASSWORD);

		final var authenticationDTO = client.post()
			.uri("/sessions")
			.body(credentials)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(AuthenticationDTO.class)
			.returnResult()
			.getResponseBody();

		token = Optional.of(authenticationDTO.getToken());
	}

	/**
	 * Execute a GET http request
	 *
	 * @param url        The url
	 * @param clazz      The class of the result
	 * @param <T>        The class to serialize the result
	 * @return          The body of the request
	 */
	protected <T> T get(final String url, final Class<T> clazz) {
		return client.get().uri(url).exchange().expectStatus().isOk().expectBody(clazz).returnResult().getResponseBody();
	}

	/**
	 * Execute a GET http request
	 *
	 * @param url          The url
	 * @param responseType The response type as ParameterizedTypeReference
	 * @param <T>          The class to serialize the result
	 * @return          The body of the request
	 */
	protected <T> T get(final String url, final ParameterizedTypeReference<T> responseType) {
		return client.get().uri(url).exchange().expectStatus().isOk().expectBody(responseType).returnResult().getResponseBody();
	}

	/**
	 * Execute a POST http request
	 *
	 * @param url        The url
	 * @param body       The body of the request
	 * @param clazz      The class of the result
	 * @param <T>        The class to serialize the result
	 * @return          The body of the request
	 */
	protected <T> T post(final String url, final Object body, final Class<T> clazz) {
		return client.post().uri(url).body(body).exchange().expectStatus().isCreated().expectBody(clazz).returnResult().getResponseBody();
	}

	/**
	 * Execute a POST http request
	 *
	 * @param url          The url
	 * @param body         The body of the request
	 * @param responseType The response type as ParameterizedTypeReference
	 * @param <T>          The class to serialize the result
	 * @return          The body of the request
	 */
	protected <T> T post(final String url, final Object body, final ParameterizedTypeReference<T> responseType) {
		return client.post().uri(url).body(body).exchange().expectStatus().isCreated().expectBody(responseType).returnResult().getResponseBody();
	}

	/**
	 * Execute a PUT http request
	 *
	 * @param url        The url
	 * @param body       The body of the request
	 * @param clazz      The class of the result
	 * @param <T>        The class to serialize the result
	 * @return          The body of the request
	 */
	protected <T> T put(final String url, final Object body, final Class<T> clazz) {
		return client.put().uri(url).body(body).exchange().expectStatus().isOk().expectBody(clazz).returnResult().getResponseBody();
	}

	/**
	 * Execute a PUT http request
	 *
	 * @param url          The url
	 * @param body         The body of the request
	 * @param responseType The response type as ParameterizedTypeReference
	 * @param <T>          The class to serialize the result
	 * @return          The body of the request
	 */
	protected <T> T put(final String url, final Object body, final ParameterizedTypeReference<T> responseType) {
		return client.put().uri(url).body(body).exchange().expectStatus().isOk().expectBody(responseType).returnResult().getResponseBody();
	}

	/**
	 * Execute a DELETE http request
	 *
	 * @param url        The url
	 * @param body       The body of the request
	 * @param clazz      The class of the result
	 * @param <T>        The class to serialize the result
	 * @return          The body of the request
	 */
	protected <T> T delete(final String url, final Object body, final Class<T> clazz) {
		return client.delete().uri(url).exchange().expectStatus().isOk().expectBody(clazz).returnResult().getResponseBody();
	}

	/**
	 * Execute a DELETE http request
	 *
	 * @param url        The url
	 * @param body       The body of the request
	 * @param clazz      The class of the result
	 * @param <T>        The class to serialize the result
	 * @return          The body of the request
	 */
	protected <T> T delete(final String url, final Object body, final ParameterizedTypeReference<T> clazz) {
		return client.delete().uri(url).exchange().expectStatus().isOk().expectBody(clazz).returnResult().getResponseBody();
	}
}
