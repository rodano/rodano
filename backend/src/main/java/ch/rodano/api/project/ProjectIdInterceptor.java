package ch.rodano.api.project;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import ch.rodano.core.services.project.ProjectIdResolver;

@Component
public class ProjectIdInterceptor implements HandlerInterceptor {

	private static final String PROJECT_ID_HEADER = "X-Project-Id";

	private final ProjectIdResolver projectIdResolver;

	public ProjectIdInterceptor(final ProjectIdResolver projectIdResolver) {
		this.projectIdResolver = projectIdResolver;
	}

	@Override
	public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {

		if("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			return true;
		}

		final String projectIdHeader = request.getHeader(PROJECT_ID_HEADER);

		if(projectIdHeader == null || projectIdHeader.trim().isEmpty()) {
			sendError(response, HttpStatus.FORBIDDEN.value(), "X-Project-Id header is required");
			return false;
		}

		final UUID headerProjectId;
		try {
			headerProjectId = UUID.fromString(projectIdHeader);
		}
		catch(IllegalArgumentException e) {
			sendError(response, HttpStatus.BAD_REQUEST.value(), "Invalid X-Project-Id format. Must be a valid UUID");
			return false;
		}

		if(!projectIdResolver.hasProject()) {
			sendError(response, HttpStatus.FORBIDDEN.value(), "No project loaded in session. Please select a project first.");
			return false;
		}

		final UUID sessionProjectId = projectIdResolver.id();
		if(!headerProjectId.equals(sessionProjectId)) {
			sendError(response, HttpStatus.FORBIDDEN.value(),
				String.format("Project mismatch: header projectId (%s) does not match session projectId (%s)",
					headerProjectId, sessionProjectId));
			return false;
		}

		return true;
	}

	private void sendError(final HttpServletResponse response, final int status, final String message) throws Exception {
		response.setStatus(status);
		response.setContentType("application/json");
		response.getWriter().write(String.format("{\"error\":\"%s\"}", message));
	}
}
