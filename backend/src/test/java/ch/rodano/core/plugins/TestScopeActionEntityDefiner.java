package ch.rodano.core.plugins;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Lazy;

import freemarker.template.Configuration;

import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.rules.entity.EntityAction;
import ch.rodano.core.model.rules.entity.EntityDefinerComponent;
import ch.rodano.core.model.rules.entity.IdentifiableEntity;
import ch.rodano.core.plugins.rules.scope.ScopeActionEntityDefiner;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.form.FormService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.scope.ScopeRelationService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.user.UserService;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.dao.event.EventDAOService;
import ch.rodano.core.services.dao.form.FormDAOService;
import ch.rodano.core.services.dao.role.RoleDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;

@EntityDefinerComponent
public final class TestScopeActionEntityDefiner extends ScopeActionEntityDefiner {

	public TestScopeActionEntityDefiner(
		@Lazy final ScopeService scopeService,
		final ScopeDAOService scopeDAOService,
		@Lazy final ScopeRelationService scopeRelationService,
		@Lazy final RoleService roleService,
		final RoleDAOService roleDAOService,
		final StudyService studyService,
		final EventDAOService eventDAOService,
		final FormDAOService formDAOService,
		@Lazy final FormService formService,
		@Lazy final EventService eventService,
		@Lazy final WorkflowStatusService workflowStatusService,
		final Configuration freemarkerConfiguration,
		@Lazy final UserService userService,
		@Lazy final ActorService actorService
	) {
		super(
			scopeService, scopeDAOService, scopeRelationService, roleService, roleDAOService, studyService, eventDAOService, formDAOService, formService, eventService, workflowStatusService,
			freemarkerConfiguration,
			userService, actorService
		);
	}

	/**
	 * Get all registered identifiable entity beans
	 *
	 * @return Registered identifiable entity beans
	 */
	@Override
	public List<IdentifiableEntity> getRegisteredBeans() {
		return List.of(
			new EntityAction() {
				@Override
				public String getId() {
					return "CHANGE CODE";
				}

				@Override
				public void action(
					final Evaluable evaluable,
					final Map<String, Object> parameters,
					final DatabaseActionContext context,
					final String message,
					final Map<String, Object> data
				) {
					logger.info("CHANGE_CODE ACTION OVERRIDE");
				}
			},
			new EntityAction() {
				@Override
				public void action(
					final Evaluable evaluable,
					final Map<String, Object> parameters,
					final DatabaseActionContext context,
					final String message,
					final Map<String, Object> data
				) {
					logger.info("TITI ACTION");
				}

				@Override
				public String getId() {
					return "TITI";
				}
			},
			new EntityAction() {
				@Override
				public void action(
					final Evaluable evaluable,
					final Map<String, Object> parameters,
					final DatabaseActionContext context,
					final String message,
					final Map<String, Object> data
				) {
					logger.info("TOTO ACTION");
				}

				@Override
				public String getId() {
					return "TOTO";
				}
			}
		);
	}
}
