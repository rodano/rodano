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

	private final StudyService studyService;

	public RobotBasicAuthenticationEntryPoint(final StudyService studyService) {
		this.studyService = studyService;
		setRealmName("Rodano");
	}

	@Override
	public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException) {
		final String realmName = studyService.isStudyLoaded()
			? "Rodano for study - " + studyService.getStudy().getLongname()
			: "Rodano";

		response.addHeader("WWW-Authenticate", "Basic realm=" + realmName);
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		logger.error("Basic authentication failed", authException);
	}
}
