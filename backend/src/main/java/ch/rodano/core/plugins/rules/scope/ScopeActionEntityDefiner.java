package ch.rodano.core.plugins.rules.scope;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.mail.MailPreparationException;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import ch.rodano.configuration.model.field.PartialDate;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.rules.Evaluable;
import ch.rodano.core.model.rules.entity.EntityAction;
import ch.rodano.core.model.rules.entity.IdentifiableEntity;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.user.User;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.form.FormService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.scope.ScopeRelationService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.user.UserService;
import ch.rodano.core.services.bll.workflowStatus.DataFamily;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.dao.event.EventDAOService;
import ch.rodano.core.services.dao.form.FormDAOService;
import ch.rodano.core.services.dao.role.RoleDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.services.plugin.entity.EntityType;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ScopeActionEntityDefiner extends AbstractScopeEntityDefiner {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected final ScopeService scopeService;
	protected final ScopeDAOService scopeDAOService;
	protected final ScopeRelationService scopeRelationService;
	protected final RoleService roleService;
	protected final RoleDAOService roleDAOService;
	protected final StudyService studyService;
	protected final EventDAOService eventDAOService;
	protected final FormDAOService formDAOService;
	protected final FormService formService;
	protected final EventService eventService;
	protected final WorkflowStatusService workflowStatusService;
	protected final Configuration freemarkerConfiguration;
	protected final UserService userService;
	protected final ActorService actorService;

	public ScopeActionEntityDefiner(
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
		this.scopeService = scopeService;
		this.scopeDAOService = scopeDAOService;
		this.scopeRelationService = scopeRelationService;
		this.roleService = roleService;
		this.roleDAOService = roleDAOService;
		this.studyService = studyService;
		this.eventDAOService = eventDAOService;
		this.formDAOService = formDAOService;
		this.formService = formService;
		this.eventService = eventService;
		this.workflowStatusService = workflowStatusService;
		this.freemarkerConfiguration = freemarkerConfiguration;
		this.userService = userService;
		this.actorService = actorService;
	}

	@Override
	public EntityType getRelatedEntityType() {
		return EntityType.ACTION;
	}

	@Override
	public List<IdentifiableEntity> getRegisteredBeans() {
		return List.of(
			new EntityAction() {
				@Override
				public void action(
					final Evaluable evaluable,
					final Map<String, Object> parameters,
					final DatabaseActionContext context,
					final String message,
					final Map<String, Object> data
				) {
					final var scope = (Scope) evaluable;
					scope.setCode((String) parameters.get("CODE"));
					scopeService.save(scope, context, "Change code");
				}

				@Override
				public String getId() {
					return "CHANGE_CODE";
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
					final var scope = (Scope) evaluable;
					scope.setShortname((String) parameters.get("SHORTNAME"));
					scopeService.save(scope, context, "Change shortname");
				}

				@Override
				public String getId() {
					return "CHANGE_SHORTNAME";
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
					final var scope = (Scope) evaluable;
					scope.setLongname((String) parameters.get("LONGNAME"));
					scopeService.save(scope, context, "Change longname");
				}

				@Override
				public String getId() {
					return "CHANGE_LONGNAME";
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
					final var scope = (Scope) evaluable;
					scope.setLocked(true);
					scopeService.save(scope, context, "Block scope");
				}

				@Override
				public String getId() {
					return "BLOCK";
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
					final var scope = (Scope) evaluable;
					scope.setLocked(false);
					scopeService.save(scope, context, "Unblock scope");
				}

				@Override
				public String getId() {
					return "UNBLOCK";
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
					final var scope = (Scope) evaluable;

					final var study = studyService.getStudy();
					final var newProfile = study.getProfile((String) parameters.get("NEW_PROFILE"));
					final var targetProfile = study.getProfile((String) parameters.get("TARGET_PROFILE"));

					// Existing roles are enabled
					final var roles = roleService.getRoles(scope, targetProfile);
					roles.forEach(Role::enable);

					// Other people have new role
					final Set<User> users = roles.stream().map(userService::getUser).collect(Collectors.toSet());

					for(final var user : users) {
						final var profiles = actorService.getActiveProfiles(user, scope);
						if(!profiles.contains(newProfile)) {
							final var userRole = new Role();
							userRole.setUserFk(user.getPk());
							userRole.setProfile(newProfile);
							userRole.setScopeFk(scope.getPk());
							userRole.enable();

							roleDAOService.saveRole(userRole, context, "Enable role");
						}
					}
				}

				@Override
				public String getId() {
					return "ENABLE_ROLE";
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
					final var scope = (Scope) evaluable;
					final var profile = studyService.getStudy().getProfile((String) parameters.get("PROFILE"));

					for(final var userRole : roleService.getActiveRoles(scope, profile)) {
						userRole.disable();
						roleDAOService.saveRole(userRole, context, "Disable role");
					}
				}

				@Override
				public String getId() {
					return "DISABLE_ROLE";
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
					final var formModel = studyService.getStudy().getFormModel((String) parameters.get("FORM_MODEL_ID"));
					final var rationale = (String) parameters.get("RATIONALE");
					final var scope = (Scope) evaluable;
					//rule can execute only if the form model is allowed for scope model
					if(scope.getScopeModel().getFormModelIds().contains(formModel.getId())) {
						final var forms = formService.getAllIncludingRemoved(scope);
						final var form = forms.stream().filter(f -> f.getFormModelId().equals(formModel.getId())).findFirst();
						//create form if it does not exist
						if(form.isEmpty()) {
							formService.create(scope, formModel, context, rationale);
						}
						//restore form otherwise
						else if(form.get().getDeleted()) {
							formService.restore(scope, Optional.empty(), form.get(), context, StringUtils.defaultIfBlank(rationale, "Create form"));
						}
						//otherwise, nothing to do
					}
				}

				@Override
				public String getId() {
					return "ADD_FORM";
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
					final var scope = (Scope) evaluable;
					final var workflow = studyService.getStudy().getWorkflow((String) parameters.get("WORKFLOW"));

					if(scope.getScopeModel().getWorkflowIds().contains(workflow.getId())) {
						final var family = new DataFamily(scope);
						workflowStatusService.create(family, scope, workflow, data, context, "Initialize workflow");
					}
				}

				@Override
				public String getId() {
					return "INITIALIZE_WORKFLOW";
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
				) throws IOException {
					final var scope = (Scope) evaluable;

					// Handle text message with velocity
					final var loader = new StringTemplateLoader();
					loader.putTemplate("log", (String) parameters.get("TEXT"));
					freemarkerConfiguration.setTemplateLoader(loader);

					final String text;
					try {
						text = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate("log"), Map.of("scope", scope));
					}
					catch(final TemplateException e) {
						throw new MailPreparationException(e);
					}

					freemarkerConfiguration.unsetTemplateLoader();

					logger.info(text);
				}

				@Override
				public String getId() {
					return "WRITE_TO_LOG";
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
					final var scope = (Scope) evaluable;
					scopeService.validateFieldsOnScope(scope, context, "Validate scope");
				}

				@Override
				public String getId() {
					return "VALIDATE";
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
					final var scope = (Scope) evaluable;
					final var eventModel = scope.getScopeModel().getEventModel((String) parameters.get("EVENT_MODEL_ID"));
					final var rationale = (String) parameters.get("RATIONALE");
					eventService.create(scope, eventModel, context, StringUtils.defaultIfBlank(rationale, "Create event"));
				}

				@Override
				public String getId() {
					return "CREATE_EVENT";
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
					final var scope = (Scope) evaluable;
					eventService.resetDates(scope, context, "Reset events' dates");
				}

				@Override
				public String getId() {
					return "RESET_EVENT_DATES";
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
					final var scope = (Scope) evaluable;
					final var scopeStartDate = parameters.get("START_DATE");
					final var partialDate = PartialDate.ofObject(scopeStartDate);

					if(partialDate.isAnchoredInTime()) {
						scope.setStartDate(partialDate.toZonedDateTime().get());
					}
				}

				@Override
				public String getId() {
					return "SET_START_DATE";
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
					final var scope = (Scope) evaluable;
					final var scopeEndDate = parameters.get("DATE");
					final var partialDate = PartialDate.ofObject(scopeEndDate);

					if(partialDate.isAnchoredInTime()) {
						scope.setStopDate(partialDate.toZonedDateTime().get());
					}
				}

				@Override
				public String getId() {
					return "SET_STOP_DATE";
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
					final var scope = (Scope) evaluable;
					scope.setStartDate(null);
				}

				@Override
				public String getId() {
					return "REMOVE_START_DATE";
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
					final var scope = (Scope) evaluable;
					scope.setStopDate(null);
				}

				@Override
				public String getId() {
					return "REMOVE_STOP_DATE";
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
					final var scope = (Scope) evaluable;
					@SuppressWarnings("unchecked")
					final Set<Scope> scopes = (Set<Scope>) parameters.get("PARENT_SCOPE");
					scopeRelationService.createRelation(scope, scopes.iterator().next(), context, "Add parent");
				}

				@Override
				public String getId() {
					return "ADD_PARENT";
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
					final var scope = (Scope) evaluable;
					@SuppressWarnings("unchecked")
					final Set<Scope> scopes = (Set<Scope>) parameters.get("PARENT_SCOPE");
					final var parentScope = scopes.iterator().next();
					final var parentRelation = scopeRelationService.getActiveRelation(scope, parentScope);
					scopeRelationService.endRelation(parentRelation, ZonedDateTime.now(), context, "Remove parent");
				}

				@Override
				public String getId() {
					return "REMOVE_PARENT";
				}
			}
		);
	}
}
