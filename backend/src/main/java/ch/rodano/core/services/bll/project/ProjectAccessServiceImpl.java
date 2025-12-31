package ch.rodano.core.services.bll.project;

import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import ch.rodano.core.model.jooq.enums.ProjectStatus;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.role.RoleService;

import static ch.rodano.core.model.jooq.tables.ProfileProfileRights.PROFILE_PROFILE_RIGHTS;

@Service
public class ProjectAccessServiceImpl implements ProjectAccessService {

	private final ProjectService projectService;
	private final RoleService roleService;
	private final DSLContext create;

	public ProjectAccessServiceImpl(final ProjectService projectService,
									final RoleService roleService,
									final DSLContext create) {
		this.projectService = projectService;
		this.roleService = roleService;
		this.create = create;
	}

	@Override
	public boolean canRead(final User user, final UUID projectId) {
		final var project = projectService.getProjectById(projectId);
		final var status = project.getStatus();

		if(status == ProjectStatus.ACTIVE) {
			return hasAnyRole(user, projectId);
		}

		if(status == ProjectStatus.CLOSED) {
			return isAllowed(user, projectId);
		}

		if(status == ProjectStatus.ARCHIVED) {
			return hasAnyRole(user, projectId);
		}

		return false;
	}

	@Override
	public boolean canWrite(final User user, final UUID projectId) {
		final var project = projectService.getProjectById(projectId);
		final var status = project.getStatus();

		if(status == ProjectStatus.ACTIVE) {
			return hasWritePermission(user, projectId);
		}

		if(status == ProjectStatus.CLOSED) {
			return isAllowed(user, projectId);
		}

		if(status == ProjectStatus.ARCHIVED) {
			return false;
		}

		return false;
	}

	private boolean hasAnyRole(final User user, final UUID projectId) {
		final var roles = roleService.getActiveRoles(user);
		return roles.stream().anyMatch(role -> projectId.equals(role.getProjectId()));
	}

	private boolean isAllowed(final User user, final UUID projectId) {
		final var roles = roleService.getRolesForUser(user.getPk(), projectId);
		return roles.stream()
			.filter(role -> projectId.equals(role.getProjectId()))
			.anyMatch(role -> {
					final var profile = role.getProfile();
					if(profile == null) {
						return false;
					}
					final var profileCode = profile.getId();
					return "ADMIN".equalsIgnoreCase(profileCode) ||
						"DATAMANAGER".equalsIgnoreCase(profileCode) ||
						"DATAENTRY_MASTER".equalsIgnoreCase(profileCode);
				}
			);
	}

	private boolean hasWritePermission(final User user, final UUID projectId) {
		final var roles = roleService.getRolesForUser(user.getPk(), projectId);

		for(final var role : roles) {
			if(!projectId.equals(role.getProjectId())) {
				continue;
			}

			final var profile = role.getProfile();
			if(profile == null) {
				continue;
			}

			final boolean hasWriteRights = create.fetchExists(
				create.selectFrom(PROFILE_PROFILE_RIGHTS)
					.where(PROFILE_PROFILE_RIGHTS.PROJECT_ID.eq(projectId))
					.and(PROFILE_PROFILE_RIGHTS.PROFILE_ID.eq(profile.getProfileId()))
					.and(PROFILE_PROFILE_RIGHTS.CAN_WRITE.eq(true))
			);

			if(hasWriteRights) {
				return true;
			}
		}

		return false;
	}
}
