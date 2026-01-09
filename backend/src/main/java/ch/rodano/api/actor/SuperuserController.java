package ch.rodano.api.actor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.rodano.core.services.bll.user.UserSecurityService;

@RestController
@RequestMapping("/api/superuser")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class SuperuserController {

	private final UserSecurityService userSecurityService;

	public SuperuserController(final UserSecurityService userSecurityService) {
		this.userSecurityService = userSecurityService;
	}
}
