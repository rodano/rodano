package ch.rodano.api.configuration.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import ch.rodano.core.services.bll.study.StudyService;

@Component
public class RobotBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public RobotBasicAuthenticationEntryPoint(final StudyService studyService) {
		setRealmName("Rodano for study - " + studyService.getStudy().getLongname());
	}

	@Override
	public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException) {
		response.addHeader("WWW-Authenticate", "Basic realm=" + getRealmName());
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		logger.error("Basic authentication failed", authException);
	}
}
