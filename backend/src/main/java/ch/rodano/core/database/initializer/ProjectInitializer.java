package ch.rodano.core.database.initializer;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.UUID;

import jakarta.transaction.Transactional;

import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.core.helpers.UserCreatorService;
import ch.rodano.core.helpers.builder.UserBuilder;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.role.RoleStatus;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.form.FormService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.user.UserSecurityService;
import ch.rodano.core.services.bll.workflowStatus.DataFamily;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.dao.audit.AuditActionService;
import ch.rodano.core.services.dao.role.RoleDAOService;
import ch.rodano.core.services.dao.user.UserDAOService;
import ch.rodano.core.services.plugin.validator.exception.BadlyFormattedValue;
import ch.rodano.core.services.plugin.validator.exception.InvalidValueException;

import static ch.rodano.core.model.jooq.tables.DatasetModel.DATASET_MODEL;
import static ch.rodano.core.model.jooq.tables.EventModel.EVENT_MODEL;
import static ch.rodano.core.model.jooq.tables.FieldModel.FIELD_MODEL;
import static ch.rodano.core.model.jooq.tables.FormModel.FORM_MODEL;
import static ch.rodano.core.model.jooq.tables.Project.PROJECT;
import static ch.rodano.core.model.jooq.tables.ScopeModel.SCOPE_MODEL;

@Service
public class ProjectInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectInitializer.class);

	public static final String RATIONALE = "Project initialized";

	private final DSLContext create;
	private final ScopeService scopeService;
	private final EventService eventService;
	private final FormService formService;
	private final DatasetService datasetService;
	private final WorkflowStatusService workflowStatusService;
	private final AuditActionService auditActionService;
	private final UserCreatorService userCreatorService;
	private final UserSecurityService userSecurityService;
	private final TestDataInitializer testDataInitializer;
	private final DemoUsersInitializer demoUsersInitializer;
	private final ObjectMapper objectMapper;
	private final UserDAOService userDAOService;
	private final RoleDAOService roleDAOService;
	private final StudyService studyService;

	public ProjectInitializer(final DSLContext create,
							  final ScopeService scopeService,
							  final EventService eventService,
							  final FormService formService,
							  final DatasetService datasetService,
							  final WorkflowStatusService workflowStatusService,
							  final AuditActionService auditActionService,
							  final UserCreatorService userCreatorService,
							  final UserSecurityService userSecurityService,
							  final TestDataInitializer testDataInitializer,
							  final DemoUsersInitializer demoUsersInitializer,
							  final ObjectMapper objectMapper,
							  final UserDAOService userDAOService,
							  final RoleDAOService roleDAOService,
							  final StudyService studyService) {
		this.create = create;
		this.scopeService = scopeService;
		this.eventService = eventService;
		this.formService = formService;
		this.datasetService = datasetService;
		this.workflowStatusService = workflowStatusService;
		this.auditActionService = auditActionService;
		this.userCreatorService = userCreatorService;
		this.userSecurityService = userSecurityService;
		this.testDataInitializer = testDataInitializer;
		this.demoUsersInitializer = demoUsersInitializer;
		this.objectMapper = objectMapper;
		this.userDAOService = userDAOService;
		this.roleDAOService = roleDAOService;
		this.studyService = studyService;
	}

	@Transactional
	public void initializeProject(final Study study, final ProjectInitOptions options) throws InvalidValueException, BadlyFormattedValue, IOException {
		LOGGER.info("Initializing project: {} ({})", study.getId(), study.getProjectId());

		final ZonedDateTime origin = options.withDemoData
			? ZonedDateTime.now().minusYears(3).truncatedTo(ChronoUnit.MILLIS)
			: ZonedDateTime.now().minusMinutes(1).truncatedTo(ChronoUnit.MILLIS);

		LOGGER.info("Loading study into StudyService");
		studyService.loadStudyForProject(study.getProjectId());

		final var context = auditActionService.createAuditActionAndGenerateContext(Actor.SYSTEM, RATIONALE, study.getProjectId());

		ensureProjectExists(study);
		ensureModelCatalogExists(study);

		final var root = createRootScope(context, origin, study);

		if(options.createAdminUser) {
			createAdminUser(study, root, options, context);
		}

		if(options.withDemoUsers) {
			LOGGER.info("Creating demo users for project {}", study.getId());
			demoUsersInitializer.initialize(options.adminEmail, options.adminPassword, context);
		}

		if(options.withDemoData) {
			LOGGER.info("Reloading study from database before creating demo data");
			studyService.reloadStudyFromDatabase();
			LOGGER.info("Creating demo data for project {}", study.getId());
			testDataInitializer.initialize(origin, root);
		}

		LOGGER.info("Project {} initialized successfully", study.getId());
	}

	private void ensureProjectExists(final Study study) throws JsonProcessingException {
		create.insertInto(PROJECT)
			.set(PROJECT.PROJECT_ID, study.getProjectId())
			.set(PROJECT.CODE, study.getId())
			.set(PROJECT.SHORTNAME, objectMapper.writeValueAsString(study.getShortname()))
			.set(PROJECT.DESCRIPTION, objectMapper.writeValueAsString(study.getDescription()))
			.set(PROJECT.URL, study.getUrl())
			.set(PROJECT.COLOR, study.getColor())
			.set(PROJECT.INTRODUCTION_TEXT, study.getIntroductionText())
			.onDuplicateKeyUpdate()
			.set(PROJECT.CODE, study.getId())
			.set(PROJECT.SHORTNAME, objectMapper.writeValueAsString(study.getShortname()))
			.set(PROJECT.DESCRIPTION, objectMapper.writeValueAsString(study.getDescription()))
			.execute();
	}

	private void ensureModelCatalogExists(final Study study) {
		final var projectId = study.getProjectId();

		// SCOPE MODELS
		study.getScopeModels().forEach(sm -> {
			create.insertInto(SCOPE_MODEL)
				.set(SCOPE_MODEL.PROJECT_ID, projectId)
				.set(SCOPE_MODEL.SCOPE_MODEL_ID, sm.getScopeModelId())
				.set(SCOPE_MODEL.CODE, sm.getId())
				.onDuplicateKeyIgnore()
				.execute();
		});

		// DATASET MODELS
		study.getDatasetModels().forEach(dm -> {
			create.insertInto(DATASET_MODEL)
				.set(DATASET_MODEL.PROJECT_ID, projectId)
				.set(DATASET_MODEL.DATASET_MODEL_ID, dm.getDatasetModelId())
				.set(DATASET_MODEL.CODE, dm.getId())
				.onDuplicateKeyIgnore()
				.execute();
		});

		// EVENT MODELS
		study.getEventModels().forEach(em -> {
			create.insertInto(EVENT_MODEL)
				.set(EVENT_MODEL.PROJECT_ID, projectId)
				.set(EVENT_MODEL.EVENT_MODEL_ID, em.getEventModelId())
				.set(EVENT_MODEL.CODE, em.getId())
				.onDuplicateKeyIgnore()
				.execute();
		});

		// FORM MODELS
		study.getFormModels().forEach(fm -> {
			create.insertInto(FORM_MODEL)
				.set(FORM_MODEL.PROJECT_ID, projectId)
				.set(FORM_MODEL.FORM_MODEL_ID, fm.getFormModelId())
				.set(FORM_MODEL.CODE, fm.getId())
				.onDuplicateKeyIgnore()
				.execute();
		});

		// FIELD MODELS
		study.getFieldModels().forEach(fm -> {
			create.insertInto(FIELD_MODEL)
				.set(FIELD_MODEL.PROJECT_ID, projectId)
				.set(FIELD_MODEL.FIELD_MODEL_ID, fm.getFieldModelId())
				.set(FIELD_MODEL.DATASET_MODEL_ID, fm.getDatasetModel().getDatasetModelId())
				.set(FIELD_MODEL.CODE, fm.getId())
				.onDuplicateKeyIgnore()
				.execute();
		});
	}

	private Scope createRootScope(final DatabaseActionContext context, final ZonedDateTime origin, final Study study) {
		final ScopeModel rootScopeModel = study.getRootScopeModel();

		final Scope root = new Scope();
		root.setProjectId(study.getProjectId());
		root.setScopeModel(rootScopeModel);
		root.setId(UUID.randomUUID().toString());
		root.setCode(study.getId());
		root.setShortname(study.getDefaultLocalizedShortname());
		root.setStartDate(origin);

		scopeService.create(root, null, context, "Create root scope");

		final var family = new DataFamily(root);
		workflowStatusService.createAll(family, root, null, context, RATIONALE);
		eventService.createAll(root, context, RATIONALE);
		datasetService.createAll(root, context, RATIONALE);
		formService.createAll(root, context, RATIONALE);

		return root;
	}

	private void createAdminUser(final Study study, final Scope root, final ProjectInitOptions options, final DatabaseActionContext context) {
		final var adminProfile = study.getProfile("ADMIN");

		final var existingUser = userDAOService.getUserByEmail(options.adminEmail);

		if(existingUser != null) {
			LOGGER.info("User {} already exists, adding role for project {}", options.adminEmail, study.getId());

			final var role = new Role();
			role.setProjectId(study.getProjectId());
			role.setProfile(adminProfile);
			role.setScopeFk(root.getPk());
			role.setUserFk(existingUser.getPk());
			role.setStatus(RoleStatus.ENABLED);

			roleDAOService.saveRole(role, context, RATIONALE);
		}
		else {
			LOGGER.info("Creating new user {} for project {}", options.adminEmail, study.getId());

			final var password = userSecurityService.encodePassword(options.adminPassword);
			final var users = new ArrayList<UserCreatorService.UserCreation>();

			users.add(UserBuilder.createUser(options.adminName, options.adminEmail)
				.setHashedPassword(password)
				.setLanguage(LanguageStatic.en)
				.addRole(root, adminProfile)
				.getUserAndRoles());

			userCreatorService.batchCreateAndEnable(users, context);
		}

	}

	public static class ProjectInitOptions {
		public boolean createAdminUser = true;
		public String adminName = "Admin User";
		public String adminEmail = "admin@example.ch";
		public String adminPassword = "Password1!";
		public boolean withDemoData = false;
		public boolean withDemoUsers = false;

		public static ProjectInitOptions defaults() {
			return new ProjectInitOptions();
		}

		public ProjectInitOptions withAdminUser(final String name, final String email, final String password) {
			this.createAdminUser = true;
			this.adminName = name;
			this.adminEmail = email;
			this.adminPassword = password;
			return this;
		}

		public ProjectInitOptions withDemoUsers() {
			this.withDemoUsers = true;
			return this;
		}

		public ProjectInitOptions withDemoData() {
			this.withDemoData = true;
			return this;
		}
	}
}
