package ch.rodano.test;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import ch.rodano.api.authentication.AuthenticationDTO;
import ch.rodano.api.authentication.CredentialsDTO;
import ch.rodano.core.database.initializer.DatabaseInitializer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ControllerTest extends DatabaseTest {

	protected final String adminOnStudyEmail = "test+test-admin@rodano.ch";
	protected final String investigatorOnStudyEmail = "test+test-investigator@rodano.ch";
	protected final String dataManagerOnStudyEmail = "test+test-datamanager@rodano.ch";

	@Value("${rodano.pagination.maximum-page-size}")
	protected Integer MAX_PAGE_SIZE;

	@Value("${server.servlet.context-path}")
	protected String contextPath;

	@Autowired
	protected TestRestTemplate restTemplate;

	/**
	 * Clear the rest template authentication
	 */
	@AfterEach
	protected void clearAuthentication() {
		restTemplate.getRestTemplate().getInterceptors().clear();
	}

	/**
	 * Authenticate with the default password
	 *
	 */
	protected void authenticate(final String email) {
		final var headers = new HttpHeaders();
		headers.set(HttpHeaders.USER_AGENT, "Integration Test");

		final var credentials = new CredentialsDTO();
		credentials.setEmail(email);
		credentials.setPassword(DatabaseInitializer.DEFAULT_PASSWORD);

		restTemplate.postForEntity("/sessions", new HttpEntity<>(credentials, headers), String.class);

		final var authDto = executePostAndReturnBody("/sessions", new HttpEntity<>(credentials, headers), AuthenticationDTO.class);
		assertNotNull(authDto);

		final var token = authDto.getToken();

		// Include token in all further requests
		clearAuthentication();
		restTemplate.getRestTemplate().setInterceptors(Collections.singletonList((request, body, execution) -> {
			request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
			return execution.execute(request, body);
		}));
	}

	/**
	 * Execute a GET http request
	 *
	 * @param url        The url
	 * @param clazz      The class of the result
	 * @param <T>        The class to serialize the result
	 * @return          The body of the request
	 */
	protected <T> T executeGetAndReturnBody(final String url, final Class<T> clazz) {
		final var response = restTemplate.getForEntity(url, clazz);
		return response.getBody();
	}

	/**
	 * Execute a GET http request
	 *
	 * @param url          The url
	 * @param responseType The response type as ParameterizedTypeReference
	 * @param <T>          The class to serialize the result
	 * @return          The body of the request
	 */
	protected <T> T executeGetAndReturnBody(final String url, final ParameterizedTypeReference<T> responseType) {
		final var response = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, responseType);
		return response.getBody();
	}

	/**
	 * Execute a GET HTTP request
	 * @param url          The url
	 * @param responseType The response type as ParameterizedTypeReference
	 * @param <T>          The class to serialize the result
	 * @return          The request entity
	 */
	protected <T> ResponseEntity<T> executeGet(final String url, final ParameterizedTypeReference<T> responseType) {
		return restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, responseType);
	}

	protected <T> ResponseEntity<T> executePost(final String url, final Object body, final Class<T> clazz) {
		return restTemplate.postForEntity(url, body, clazz);
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
	protected <T> T executePostAndReturnBody(final String url, final Object body, final Class<T> clazz) {
		final var response = restTemplate.postForEntity(url, body, clazz);
		return response.getBody();
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
	protected <T> T executePostAndReturnBody(final String url, final Object body, final ParameterizedTypeReference<T> responseType) {
		final var response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body), responseType);
		return response.getBody();
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
	protected <T> T executePutAndReturnBody(final String url, final Object body, final Class<T> clazz) {
		final var response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(body), clazz);
		return response.getBody();
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
	protected <T> T executePutAndReturnBody(final String url, final Object body, final ParameterizedTypeReference<T> responseType) {
		final var response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(body), responseType);
		return response.getBody();
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
	protected <T> T executeDeleteAndReturnBody(final String url, final Object body, final Class<T> clazz) {
		final var response = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(body), clazz);
		return response.getBody();
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
	protected <T> T executeDeleteAndReturnBody(final String url, final Object body, final ParameterizedTypeReference<T> clazz) {
		final var response = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(body), clazz);
		return response.getBody();
	}
}
