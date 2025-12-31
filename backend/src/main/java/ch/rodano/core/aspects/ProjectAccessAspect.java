package ch.rodano.core.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ch.rodano.api.exception.http.ForbiddenOperationException;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.project.ProjectAccessService;
import ch.rodano.core.services.project.ProjectIdResolver;

@Aspect
@Component
public class ProjectAccessAspect {

	private final ProjectAccessService projectAccessService;
	private final ProjectIdResolver projectIdResolver;

	public ProjectAccessAspect(final ProjectAccessService projectAccessService, final ProjectIdResolver projectIdResolver) {
		this.projectAccessService = projectAccessService;
		this.projectIdResolver = projectIdResolver;
	}

	@Before("(" +
		"@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
		"@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
		"@annotation(org.springframework.web.bind.annotation.DeleteMapping)" +
		") && " +
		"execution(* ch.rodano.api..*(..))")
	public void checkWriteAccess(final JoinPoint joinPoint) {
		final var signature = (MethodSignature) joinPoint.getSignature();
		final var method = signature.getMethod();

		if(method.isAnnotationPresent(SkipProjectAccessCheck.class)) {
			return;
		}

		if(!projectIdResolver.hasProject()) {
			return;
		}

		final var auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth == null || !(auth.getPrincipal() instanceof User user)) {
			return;
		}

		final var projectId = projectIdResolver.id();

		if(!projectAccessService.canWrite(user, projectId)) {
			throw new ForbiddenOperationException("No write access to this project in status: restricted");
		}
	}
}
