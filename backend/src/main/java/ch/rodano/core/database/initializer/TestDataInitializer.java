package ch.rodano.core.database.initializer;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.rules.Operator;
import ch.rodano.configuration.model.workflow.WorkflowAction;
import ch.rodano.core.helpers.FieldSubmitterHelper;
import ch.rodano.core.helpers.ScopeCreatorService;
import ch.rodano.core.helpers.UserCreatorService;
import ch.rodano.core.helpers.builder.ScopeBuilder;
import ch.rodano.core.helpers.builder.UserBuilder;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.enrollment.EnrollmentModel;
import ch.rodano.core.model.resource.Resource;
import ch.rodano.core.model.robot.Robot;
import ch.rodano.core.model.rules.data.DataState;
import ch.rodano.core.model.scope.EnrollmentType;
import ch.rodano.core.model.scope.FieldModelCriterion;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.field.ValidationService;
import ch.rodano.core.services.bll.file.FileService;
import ch.rodano.core.services.bll.form.FormService;
import ch.rodano.core.services.bll.resource.ResourceService;
import ch.rodano.core.services.bll.robot.RobotService;
import ch.rodano.core.services.bll.scope.ScopeRelationService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.user.UserSecurityService;
import ch.rodano.core.services.dao.audit.AuditActionService;
import ch.rodano.core.services.dao.dataset.DatasetDAOService;
import ch.rodano.core.services.dao.form.FormDAOService;
import ch.rodano.core.services.dao.robot.RobotDAOService;
import ch.rodano.core.services.dao.user.UserDAOService;
import ch.rodano.core.services.plugin.validator.exception.BadlyFormattedValue;
import ch.rodano.core.services.plugin.validator.exception.InvalidValueException;
import ch.rodano.core.services.rule.RuleService;

@Service
public class TestDataInitializer {

	private static DateTimeFormatter DATE_FIELD_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	private final AuditActionService auditActionService;
	private final StudyService studyService;
	private final RuleService ruleService;
	private final UserDAOService userDAOService;
	private final FormDAOService formDAOService;
	private final ScopeService scopeService;
	private final ScopeRelationService scopeRelationService;
	private final EventService eventService;
	private final FormService formService;
	private final DatasetService datasetService;
	private final DatasetDAOService datasetDAOService;
	private final FileService fileService;
	private final FieldService fieldService;
	private final ValidationService validationService;
	private final ResourceService resourceService;
	private final UserCreatorService userCreatorService;
	private final ScopeCreatorService scopeCreatorService;
	private final RobotService robotService;
	private final RobotDAOService robotDAOService;

	public TestDataInitializer(
		final AuditActionService auditActionService,
		final StudyService studyService,
		final RuleService ruleService,
		final UserDAOService userDAOService,
		final FormDAOService formDAOService,
		final ScopeService scopeService,
		final ScopeRelationService scopeRelationService,
		final EventService eventService,
		final FormService formService,
		final DatasetService datasetService,
		final DatasetDAOService datasetDAOService,
		final FileService fileService,
		final FieldService fieldService,
		final ValidationService validationService,
		final ResourceService resourceService,
		final UserCreatorService userCreatorService,
		final ScopeCreatorService scopeCreatorService,
		final RobotService robotService,
		final RobotDAOService robotDAOService
	) {
		super();
		this.auditActionService = auditActionService;
		this.studyService = studyService;
		this.ruleService = ruleService;
		this.userDAOService = userDAOService;
		this.formDAOService = formDAOService;
		this.scopeService = scopeService;
		this.scopeRelationService = scopeRelationService;
		this.eventService = eventService;
		this.formService = formService;
		this.datasetService = datasetService;
		this.datasetDAOService = datasetDAOService;
		this.fileService = fileService;
		this.fieldService = fieldService;
		this.validationService = validationService;
		this.resourceService = resourceService;
		this.userCreatorService = userCreatorService;
		this.scopeCreatorService = scopeCreatorService;
		this.robotService = robotService;
		this.robotDAOService = robotDAOService;
	}

	private Robot createAndSaveRobot(final String name, final Scope scope, final Profile profile, final DatabaseActionContext context) {
		final var robot = new Robot();
		robot.setName(name);
		robot.setKey(RandomStringUtils.randomAlphanumeric(32));

		robotService.createRobot(
			robot,
			profile,
			scope,
			context,
			DatabaseInitializer.RATIONALE
		);

		return robot;
	}

	public void initialize(final ZonedDateTime origin) throws IOException,
		InvalidValueException, BadlyFormattedValue {

		final var creator = userDAOService.getUserByEmail(DatabaseInitializer.TEST_USER_EMAIL);
		final var actionDate = origin.plusMonths(3);
		final var root = scopeService.getRootScope();
		final var study = studyService.getStudy();
		final var adminProfile = study.getProfile("ADMIN");

		var context = auditActionService.createAuditActionAndGenerateContext(Actor.SYSTEM, DatabaseInitializer.RATIONALE, actionDate);

		//add the robots
		createAndSaveRobot("Bender", root, adminProfile, context);
		createAndSaveRobot("R2-D2", root, adminProfile, context);
		createAndSaveRobot("C-3PO", root, adminProfile, context);
		createAndSaveRobot("T-1000", root, adminProfile, context);
		createAndSaveRobot("Wall-E", root, adminProfile, context);

		final var robot = createAndSaveRobot("rodano-ssoproxy", root, adminProfile, context);
		robot.setKey("TG53KhK4nSEJSWoaeubvMmXlEPYBaFGJ");
		robotDAOService.saveRobot(robot, context, DatabaseInitializer.RATIONALE);

		//add countries
		final var country = study.getScopeModel("COUNTRY");
		final var austria = scopeCreatorService.createScope(new ScopeBuilder(context, origin).createScope(country, root, "AT", "Austria"));
		final var france = scopeCreatorService.createScope(new ScopeBuilder(context, origin).createScope(country, root, "FR", "France"));

		//add centers
		final var center = study.getScopeModel("CENTER");
		final var at01 = scopeCreatorService.createScope(new ScopeBuilder(context, origin).createScope(center, austria, "AT-01", "Austria 1"));
		final var at02 = scopeCreatorService.createScope(new ScopeBuilder(context, origin).createScope(center, austria, "AT-02", "Austria 2"));
		final var fr01 = scopeCreatorService.createScope(new ScopeBuilder(context, origin).createScope(center, france, "FR-01", "Centre Hospitalier de France 1"));
		final var fr02 = scopeCreatorService.createScope(new ScopeBuilder(context, origin).createScope(center, france, "FR-02", "Centre Hospitalier de France 2"));

		//add cohort
		final var cohort = study.getScopeModel("COHORT");
		final var co1 = scopeCreatorService.createScope(new ScopeBuilder(context, origin).createScope(cohort, root, "FEMALES", "Females"));

		final var co1Enrollement = new EnrollmentModel();
		co1Enrollement.setSystem(true);
		co1Enrollement.setType(EnrollmentType.AUTOMATIC);

		final var gender = study.getDatasetModel("PATIENT_DOCUMENTATION").getFieldModel("GENDER");
		final var co1Criterion = new FieldModelCriterion(gender, Operator.EQUALS, "FEMALE");
		co1Enrollement.setCriteria(Collections.singletonList(co1Criterion));

		co1.getData().setEnrollmentModel(co1Enrollement);
		scopeService.save(co1, context, DatabaseInitializer.RATIONALE);

		final var co2 = scopeCreatorService.createScope(new ScopeBuilder(context, origin).createScope(cohort, root, "YOUNG", "Young people"));

		final var co2Enrollement = new EnrollmentModel();
		co2Enrollement.setSystem(true);
		co2Enrollement.setType(EnrollmentType.AUTOMATIC);
		final var birthDate = study.getDatasetModel("PATIENT_DOCUMENTATION").getFieldModel("BIRTH_DATE");
		final var co2Criterion = new FieldModelCriterion(birthDate, Operator.GREATER_EQUALS, "1975");
		co2Enrollement.setCriteria(Collections.singletonList(co2Criterion));

		co2.getData().setEnrollmentModel(co2Enrollement);
		scopeService.save(co2, context, DatabaseInitializer.RATIONALE);

		//add patients
		final var patient = study.getScopeModel("PATIENT");
		scopeCreatorService.createScope(new ScopeBuilder(context, origin.plusDays(RandomUtils.nextLong(0, 30))).createScope(patient, at01, "AT-01-01", "AT-01-01"));
		final var fr0101 = scopeCreatorService.createScope(new ScopeBuilder(context, origin.plusDays(RandomUtils.nextLong(0, 30))).createScope(patient, fr01, "FR-01-01", "FR-01-01"));
		scopeCreatorService.createScope(new ScopeBuilder(context, origin.plusDays(RandomUtils.nextLong(0, 30))).createScope(patient, fr01, "FR-01-02", "FR-01-02"));
		final var fr0103 = scopeCreatorService.createScope(new ScopeBuilder(context, origin.plusDays(RandomUtils.nextLong(0, 30))).createScope(patient, fr01, "FR-01-03", "FR-01-03"));

		//add some data
		final var baselineEvent = patient.getEventModel("BASELINE");
		final var visit6Event = patient.getEventModel("VISIT_6");
		final var visit12Event = patient.getEventModel("VISIT_12");

		final var patientDatasetModel = study.getDatasetModel("PATIENT_DOCUMENTATION");
		final var visitDatasetModel = study.getDatasetModel("VISIT_DOCUMENTATION");
		final var studyEntryDatasetModel = study.getDatasetModel("STUDY_ENTRY");
		final var dmtDatasetModel = study.getDatasetModel("DMT_GRID");
		final var relapseDatasetModel = study.getDatasetModel("RELAPSES_GRID");

		//fr0101
		var baseline = eventService.get(fr0101, baselineEvent, 0);

		//study entry inputs
		var patientDocumentation = datasetService.get(fr0101, patientDatasetModel);
		var submitter = new FieldSubmitterHelper(context, fr0101, Optional.of(baseline), fieldService, validationService);
		final Map<String, String> inputs = new HashMap<>();
		inputs.put("GENDER", "FEMALE");
		inputs.put("BIRTH_DATE", "1980");
		inputs.put("EDUCATION", "COLLEGE");
		inputs.put("MARITAL_STATUS", "SINGLE");
		inputs.put("EMPLOYMENT", "EMPLOYED");
		inputs.put("EMPLOYMENT_TYPE", "FULL_TIME");
		inputs.put("EYES_COLOR", "Brown");
		submitter.updateFields(patientDocumentation, inputs).submit(DatabaseInitializer.RATIONALE);

		submitter = new FieldSubmitterHelper(context, fr0101, Optional.empty(), fieldService, validationService);
		submitter.updateField(patientDocumentation, "NUMBER_RELAPSE_FIRST_10_YEARS", "1");
		submitter.updateField(patientDocumentation, "FOLLOW_TREATMENT", "N");
		submitter.updateField(patientDocumentation, "HISTORY_OF_DMT", "N");
		submitter.updateField(patientDocumentation, "DATE_OF_FIRST_SYMPTOMS", "06.2000");
		submitter.updateField(patientDocumentation, "DATE_OF_DIAGNOSIS", "07.2000");
		submitter.updateField(patientDocumentation, "DATE_OF_FIRST_STUDY_DRUG", "20.10.2001");
		submitter.submit(DatabaseInitializer.RATIONALE);

		var studyEntryDataset = datasetService.get(baseline, studyEntryDatasetModel);
		submitter = new FieldSubmitterHelper(context, fr0101, Optional.of(baseline), fieldService, validationService);
		inputs.clear();
		inputs.put("ELIGIBILITY_CRITERIA", "Y");
		inputs.put("DATE_OF_ENROLLMENT", origin.plusDays(RandomUtils.nextInt(1, 30)).format(DATE_FIELD_FORMATTER));
		submitter.updateFields(studyEntryDataset, inputs).submit(DatabaseInitializer.RATIONALE);

		//study entry workflow
		var studyEntry = formService.get(baseline, "STUDY_ENTRY");
		formDAOService.saveForm(studyEntry, context, DatabaseInitializer.RATIONALE);

		var state = new DataState(fr01, Optional.of(baseline), studyEntry);
		ruleService.execute(state, study.getEventActions().get(WorkflowAction.SAVE_FORM), context);
		ruleService.execute(state, studyEntry.getFormModel().getRules(), context);

		//fr0103
		var chronology = actionDate.plusDays(RandomUtils.nextInt(1, 30));
		context = auditActionService.createAuditActionAndGenerateContext(Actor.SYSTEM, DatabaseInitializer.RATIONALE, chronology);

		//demographics entry inputs
		patientDocumentation = datasetService.get(fr0103, patientDatasetModel);
		submitter = new FieldSubmitterHelper(context, fr0103, Optional.empty(), fieldService, validationService);
		inputs.clear();
		inputs.put("GENDER", "MALE");
		inputs.put("BIRTH_DATE", "1975");
		inputs.put("EDUCATION", "COLLEGE");
		inputs.put("MARITAL_STATUS", "SINGLE");
		inputs.put("EMPLOYMENT", "STUDENT");
		inputs.put("EYES_COLOR", "Blue");
		submitter.updateFields(patientDocumentation, inputs).submit(DatabaseInitializer.RATIONALE);

		//ms history inputs
		submitter = new FieldSubmitterHelper(context, fr0103, Optional.empty(), fieldService, validationService);
		submitter.updateField(patientDocumentation, "NUMBER_RELAPSE_FIRST_10_YEARS", "2");
		submitter.updateField(patientDocumentation, "FOLLOW_TREATMENT", "Y");
		submitter.updateField(patientDocumentation, "HISTORY_OF_DMT", "Y");
		submitter.updateField(patientDocumentation, "DATE_OF_FIRST_SYMPTOMS", "06.2010");
		submitter.updateField(patientDocumentation, "DATE_OF_DIAGNOSIS", "07.2010");
		submitter.updateField(patientDocumentation, "DATE_OF_FIRST_STUDY_DRUG", "20.10.2010");
		submitter.submit(DatabaseInitializer.RATIONALE);

		//ms history workflow
		final var msHistory = formService.get(fr0103, "MS_HISTORY");
		formDAOService.saveForm(msHistory, context, DatabaseInitializer.RATIONALE);

		state = new DataState(fr0103, Optional.empty(), msHistory);
		ruleService.execute(state, study.getEventActions().get(WorkflowAction.SAVE_FORM), context);
		ruleService.execute(state, msHistory.getFormModel().getRules(), context);

		final var dmt1 = datasetService.create(fr0103, dmtDatasetModel, context, DatabaseInitializer.RATIONALE);
		submitter = new FieldSubmitterHelper(context, fr0103, Optional.empty(), fieldService, validationService);
		submitter.updateField(dmt1, "DMT_TREATMENT", "AVONEX");
		submitter.updateField(dmt1, "DMT_BEGIN_DATE", "10.2011");
		submitter.updateField(dmt1, "DMT_ONGOING", "true");
		submitter.submit(DatabaseInitializer.RATIONALE);

		datasetDAOService.saveDataset(dmt1, context, DatabaseInitializer.RATIONALE);

		final var dmt2 = datasetService.create(fr0103, dmtDatasetModel, context, DatabaseInitializer.RATIONALE);
		submitter = new FieldSubmitterHelper(context, fr0103, Optional.empty(), fieldService, validationService);
		submitter.updateField(dmt2, "DMT_TREATMENT", "BETAFERON");
		submitter.updateField(dmt2, "DMT_BEGIN_DATE", "10.2014");
		submitter.updateField(dmt2, "DMT_ONGOING", "false");
		submitter.updateField(dmt2, "DMT_END_DATE", "08.2015");
		submitter.submit(DatabaseInitializer.RATIONALE);

		datasetDAOService.saveDataset(dmt2, context, DatabaseInitializer.RATIONALE);

		//dmt workflow
		final var dmt = formService.get(fr0103, "DMT");
		formDAOService.saveForm(dmt, context, DatabaseInitializer.RATIONALE);

		state = new DataState(fr0103, Optional.empty(), dmt);
		ruleService.execute(state, study.getEventActions().get(WorkflowAction.SAVE_FORM), context);
		ruleService.execute(state, dmt.getFormModel().getRules(), context);

		//study entry inputs
		baseline = eventService.get(fr0103, baselineEvent, 0);
		studyEntryDataset = datasetService.get(baseline, studyEntryDatasetModel);

		//add file
		try(var certificateFile = getClass().getResourceAsStream("/files/certificate.jpg")) {
			final var certificateFieldModel = studyEntryDataset.getDatasetModel().getFieldModel("CONSENT_CERTIFICATE");
			final var certificateField = fieldService.get(studyEntryDataset, certificateFieldModel);
			final var certificate = fileService.create(creator, fr0103, Optional.of(baseline), studyEntryDataset, certificateField, "certificate.jpg", certificateFile, context, "Upload file");
			fileService.saveFile(certificate, context, "Submit file");
		}

		studyEntryDataset = datasetService.get(baseline, studyEntryDatasetModel);
		submitter = new FieldSubmitterHelper(context, fr0103, Optional.of(baseline), fieldService, validationService);
		inputs.clear();
		inputs.put("ELIGIBILITY_CRITERIA", "Y");
		inputs.put("DATE_OF_ENROLLMENT", chronology.format(DATE_FIELD_FORMATTER));
		inputs.put("CONSENT_CERTIFICATE", "certificate.png");
		inputs.put("CONSENT_CONFIRMED_BY", "Doctor Jones");
		submitter.updateFields(studyEntryDataset, inputs).submit(DatabaseInitializer.RATIONALE);

		//study entry workflow
		studyEntry = formService.get(baseline, "STUDY_ENTRY");
		formDAOService.saveForm(studyEntry, context, DatabaseInitializer.RATIONALE);

		state = new DataState(fr0103, Optional.of(baseline), studyEntry);
		ruleService.execute(state, study.getEventActions().get(WorkflowAction.SAVE_FORM), context);
		ruleService.execute(state, studyEntry.getFormModel().getRules(), context);

		var visitDocumentation = datasetService.get(baseline, visitDatasetModel);

		//demographics workflow
		final var demographics = formService.get(fr0103, "DEMOGRAPHICS");
		formDAOService.saveForm(demographics, context, DatabaseInitializer.RATIONALE);

		state = new DataState(fr0103, Optional.empty(), demographics);
		ruleService.execute(state, study.getEventActions().get(WorkflowAction.SAVE_FORM), context);
		ruleService.execute(state, studyEntry.getFormModel().getRules(), context);

		//edss inputs
		visitDocumentation = datasetService.get(baseline, visitDatasetModel);
		submitter = new FieldSubmitterHelper(context, fr0103, Optional.of(baseline), fieldService, validationService);
		inputs.clear();
		inputs.put("KFS1", "3");
		inputs.put("KFS2", "3");
		inputs.put("KFS3", "1");
		inputs.put("KFS4", "3");
		inputs.put("KFS5", "2");
		inputs.put("KFS6", "3");
		inputs.put("KFS7", "1");
		inputs.put("AMBULATION", "5");
		inputs.put("EDSS_SCORE", "3.0");
		submitter.updateFields(visitDocumentation, inputs).submit(DatabaseInitializer.RATIONALE);

		//edss workflow
		var edss = formService.get(baseline, "EDSS");
		formDAOService.saveForm(edss, context, DatabaseInitializer.RATIONALE);

		state = new DataState(fr0103, Optional.of(baseline), edss);
		ruleService.execute(state, study.getEventActions().get(WorkflowAction.SAVE_FORM), context);
		ruleService.execute(state, edss.getFormModel().getRules(), context);

		final var visit6 = eventService.get(fr0103, visit6Event, 0);
		chronology = chronology.plusMonths(6).plusDays(RandomUtils.nextInt(0, 5));
		context = auditActionService.createAuditActionAndGenerateContext(Actor.SYSTEM, DatabaseInitializer.RATIONALE, chronology);

		//study status inputs
		visitDocumentation = datasetService.get(visit6, visitDatasetModel);
		submitter = new FieldSubmitterHelper(context, fr0103, Optional.of(visit6), fieldService, validationService);
		inputs.clear();
		inputs.put("DATE_OF_VISIT", chronology.format(DATE_FIELD_FORMATTER));
		inputs.put("ACTUAL_MS_COURSE", "RR");
		inputs.put("WITHDRAWAL", "N");
		submitter.updateFields(visitDocumentation, inputs).submit(DatabaseInitializer.RATIONALE);

		//study status workflow
		var studyStatus = formService.get(visit6, "STUDY_STATUS");
		formDAOService.saveForm(studyStatus, context, DatabaseInitializer.RATIONALE);

		state = new DataState(fr0103, Optional.of(baseline), studyStatus);
		ruleService.execute(state, study.getEventActions().get(WorkflowAction.SAVE_FORM), context);
		ruleService.execute(state, studyStatus.getFormModel().getRules(), context);

		//edss inputs
		visitDocumentation = datasetService.get(visit6, visitDatasetModel);
		submitter = new FieldSubmitterHelper(context, fr0103, Optional.of(visit6), fieldService, validationService);
		inputs.clear();
		inputs.put("KFS1", "3");
		inputs.put("KFS2", "4");
		inputs.put("KFS3", "1");
		inputs.put("KFS4", "3");
		inputs.put("KFS5", "2");
		inputs.put("KFS6", "3");
		inputs.put("KFS7", "3");
		inputs.put("AMBULATION", "5");
		inputs.put("EDSS_SCORE", "3.5");
		submitter.updateFields(visitDocumentation, inputs).submit(DatabaseInitializer.RATIONALE);

		//edss workflow
		edss = formService.get(visit6, "EDSS");
		formDAOService.saveForm(edss, context, DatabaseInitializer.RATIONALE);

		state = new DataState(fr0103, Optional.of(visit6), edss);
		ruleService.execute(state, study.getEventActions().get(WorkflowAction.SAVE_FORM), context);
		ruleService.execute(state, edss.getFormModel().getRules(), context);

		//relapses inputs
		visitDocumentation = datasetService.get(visit6, visitDatasetModel);
		submitter = new FieldSubmitterHelper(context, fr0103, Optional.of(visit6), fieldService, validationService);
		submitter.updateField(visitDocumentation, "RELAPSES_SINCE_LV", "Y");
		submitter.submit(DatabaseInitializer.RATIONALE);

		final var relapse1 = datasetService.create(fr0103, visit6, relapseDatasetModel, context, DatabaseInitializer.RATIONALE);
		submitter = new FieldSubmitterHelper(context, fr0103, Optional.of(visit6), fieldService, validationService);
		submitter.updateField(relapse1, "RLP_START_DATE", "26.12.2013");
		submitter.updateField(relapse1, "RLP_DATE_MAX_SEV", "30.12.2013");
		submitter.updateField(relapse1, "RLP_SEVERITY", "Moderate");
		submitter.updateField(relapse1, "RLP_IMPACT", "Unknown");
		submitter.updateField(relapse1, "RLP_RECOVERY", "Complete");
		submitter.submit(DatabaseInitializer.RATIONALE);

		datasetDAOService.saveDataset(relapse1, context, DatabaseInitializer.RATIONALE);

		final var relapse2 = datasetService.create(fr0103, visit6, relapseDatasetModel, context, DatabaseInitializer.RATIONALE);
		submitter = new FieldSubmitterHelper(context, fr0103, Optional.of(visit6), fieldService, validationService);
		submitter.updateField(relapse2, "RLP_START_DATE", "03.10.2014");
		submitter.updateField(relapse2, "RLP_DATE_MAX_SEV", "10.10.2014");
		submitter.updateField(relapse2, "RLP_SEVERITY", "Severe");
		submitter.updateField(relapse2, "RLP_IMPACT", "Y");
		submitter.updateField(relapse2, "RLP_RECOVERY", "Partial");
		submitter.submit(DatabaseInitializer.RATIONALE);

		datasetDAOService.saveDataset(relapse2, context, DatabaseInitializer.RATIONALE);

		final var relapse3 = datasetService.create(fr0103, visit6, relapseDatasetModel, context, DatabaseInitializer.RATIONALE);
		submitter = new FieldSubmitterHelper(context, fr0103, Optional.of(visit6), fieldService, validationService);
		submitter.updateField(relapse3, "RLP_START_DATE", "12.02.2016");
		submitter.updateField(relapse3, "RLP_DATE_MAX_SEV", "14.02.2016");
		submitter.updateField(relapse3, "RLP_SEVERITY", "Severe");
		submitter.updateField(relapse3, "RLP_IMPACT", "Y");
		submitter.updateField(relapse3, "RLP_RECOVERY", "Partial");
		submitter.submit(DatabaseInitializer.RATIONALE);

		datasetDAOService.saveDataset(relapse3, context, DatabaseInitializer.RATIONALE);

		//relapse workflow
		var relapse = formService.get(visit6, "RELAPSES");
		formDAOService.saveForm(relapse, context, DatabaseInitializer.RATIONALE);

		state = new DataState(fr0103, Optional.of(visit6), relapse);
		ruleService.execute(state, study.getEventActions().get(WorkflowAction.SAVE_FORM), context);
		ruleService.execute(state, relapse.getFormModel().getRules(), context);

		//transfer fr0103
		scopeRelationService.transfer(fr0103, fr02, chronology.plusMonths(3), context);

		final var visit12 = eventService.get(fr0103, visit12Event, 0);
		chronology = chronology.plusMonths(6).plusDays(RandomUtils.nextInt(0, 5));
		context = auditActionService.createAuditActionAndGenerateContext(Actor.SYSTEM, DatabaseInitializer.RATIONALE, chronology);

		//study status inputs
		visitDocumentation = datasetService.get(visit12, visitDatasetModel);
		submitter = new FieldSubmitterHelper(context, fr0103, Optional.of(visit12), fieldService, validationService);
		inputs.clear();
		inputs.put("DATE_OF_VISIT", chronology.format(DATE_FIELD_FORMATTER));
		inputs.put("ACTUAL_MS_COURSE", "RR");
		inputs.put("WITHDRAWAL", "N");
		submitter.updateFields(visitDocumentation, inputs).submit(DatabaseInitializer.RATIONALE);

		//study status workflow
		studyStatus = formService.get(visit12, "STUDY_STATUS");
		formDAOService.saveForm(studyStatus, context, DatabaseInitializer.RATIONALE);

		state = new DataState(fr0103, Optional.of(visit12), studyStatus);
		ruleService.execute(state, study.getEventActions().get(WorkflowAction.SAVE_FORM), context);
		ruleService.execute(state, studyStatus.getFormModel().getRules(), context);

		//edss inputs
		visitDocumentation = datasetService.get(visit12, visitDatasetModel);
		submitter = new FieldSubmitterHelper(context, fr0103, Optional.of(visit12), fieldService, validationService);
		inputs.clear();
		inputs.put("KFS1", "4");
		inputs.put("KFS2", "4");
		inputs.put("KFS3", "2");
		inputs.put("KFS4", "3");
		inputs.put("KFS5", "2");
		inputs.put("KFS6", "3");
		inputs.put("KFS7", "3");
		inputs.put("AMBULATION", "5");
		inputs.put("EDSS_SCORE", "3.5");
		submitter.updateFields(visitDocumentation, inputs).submit(DatabaseInitializer.RATIONALE);

		//edss workflow
		edss = formService.get(visit12, "EDSS");
		formDAOService.saveForm(edss, context, DatabaseInitializer.RATIONALE);

		state = new DataState(fr0103, Optional.of(visit12), edss);
		ruleService.execute(state, study.getEventActions().get(WorkflowAction.SAVE_FORM), context);
		ruleService.execute(state, edss.getFormModel().getRules(), context);

		//relapses inputs
		visitDocumentation = datasetService.get(visit12, visitDatasetModel);
		submitter = new FieldSubmitterHelper(context, fr0103, Optional.of(visit12), fieldService, validationService);
		submitter.updateField(visitDocumentation, "RELAPSES_SINCE_LV", "Y");
		submitter.submit(DatabaseInitializer.RATIONALE);

		//relapse workflow
		relapse = formService.get(visit12, "RELAPSES");
		formDAOService.saveForm(relapse, context, DatabaseInitializer.RATIONALE);

		state = new DataState(fr0103, Optional.of(visit12), relapse);
		ruleService.execute(state, study.getEventActions().get(WorkflowAction.SAVE_FORM), context);
		ruleService.execute(state, relapse.getFormModel().getRules(), context);

		final var encodedDefaultPassword = new BCryptPasswordEncoder(UserSecurityService.BCRYPT_STRENGTH).encode(DatabaseInitializer.DEFAULT_PASSWORD);

		final var dataEntryMasterUser = UserBuilder.createUser("DataEntry Master", "test+dmaster@rodano.ch")
			.setHashedPassword(encodedDefaultPassword)
			.addRole(root, study.getProfile("DATAENTRY_MASTER"))
			.addRole(root, study.getProfile("DATAENTRY_A"))
			.addRole(root, study.getProfile("DATAENTRY_B"))
			.getUserAndRoles();
		final var dataEntryMaster = userCreatorService.createAndEnable(dataEntryMasterUser, context);

		final var dataManagerUser = UserBuilder.createUser("Data Manager", "test+dm@rodano.ch")
			.setHashedPassword(encodedDefaultPassword)
			.addRole(root, study.getProfile("DATAMANAGER"))
			.getUserAndRoles();
		final var dataManager = userCreatorService.createAndEnable(dataManagerUser, context);

		//create default users
		final var defaultUsers = new ArrayList<UserCreatorService.UserCreation>();
		defaultUsers.add(
			UserBuilder.createUser("Investigator", "test+iinves@rodano.ch")
				.setHashedPassword(encodedDefaultPassword)
				.addRole(fr01, study.getProfile("INVESTIGATOR"))
				.addRole(fr01, study.getProfile("ESIGNATURE"))
				.getUserAndRoles()
		);
		defaultUsers.add(
			UserBuilder.createUser("Principal Investigator", "test+pinves@rodano.ch")
				.setHashedPassword(encodedDefaultPassword)
				.addRole(fr02, study.getProfile("PRINCIPAL_INVESTIGATOR"))
				.addRole(fr01, study.getProfile("ESIGNATURE"))
				.getUserAndRoles()
		);
		defaultUsers.add(
			UserBuilder.createUser("Sponsor", "test+sponsor@rodano.ch")
				.setHashedPassword(encodedDefaultPassword)
				.addRole(root, study.getProfile("SPONSOR"))
				.getUserAndRoles()
		);
		defaultUsers.add(
			UserBuilder.createUser("DataEntry A", "test+dentrya@rodano.ch")
				.setHashedPassword(encodedDefaultPassword)
				.addRole(root, study.getProfile("DATAENTRY_A"))
				.getUserAndRoles()
		);
		defaultUsers.add(
			UserBuilder.createUser("DataEntry B", "test+dentryb@rodano.ch")
				.setHashedPassword(encodedDefaultPassword)
				.addRole(root, study.getProfile("DATAENTRY_B"))
				.getUserAndRoles()
		);

		//save the default users
		userCreatorService.batchCreateAndEnable(defaultUsers, context);

		//add resources
		final Resource resourceStudy1 = new Resource();
		resourceStudy1.setCategoryId("DOCUMENTS");
		resourceStudy1.setTitle("Annual report 2019");
		resourceStudy1.setDescription("See attached document.");
		resourceStudy1.setPublicResource(false);
		resourceStudy1.setScopeFk(root.getPk());
		resourceStudy1.setUserFk(creator.getPk());
		resourceService.createResource(resourceStudy1, dataManager, context);

		final Resource resourceStudy2 = new Resource();
		resourceStudy2.setCategoryId("NEWS");
		resourceStudy2.setTitle("Starting phase II");
		resourceStudy2.setDescription("Here come the details of what this news entails.");
		resourceStudy2.setPublicResource(false);
		resourceStudy2.setScopeFk(root.getPk());
		resourceStudy2.setUserFk(creator.getPk());
		resourceService.createResource(resourceStudy2, dataManager, context);

		final Resource resourceCountry1SpreadToDescendants = new Resource();
		resourceCountry1SpreadToDescendants.setCategoryId("DOCUMENTS");
		resourceCountry1SpreadToDescendants.setTitle("Ethical charter");
		resourceCountry1SpreadToDescendants.setDescription("Guidelines concerning this study.");
		resourceCountry1SpreadToDescendants.setPublicResource(false);
		resourceCountry1SpreadToDescendants.setScopeFk(austria.getPk());
		resourceCountry1SpreadToDescendants.setUserFk(dataEntryMaster.getPk());
		resourceService.createResource(resourceCountry1SpreadToDescendants, dataManager, context);

		final Resource resourceCountry2Public = new Resource();
		resourceCountry2Public.setCategoryId("DOCUMENTS");
		resourceCountry2Public.setTitle("User guide");
		resourceCountry2Public.setDescription("This is the user guide to download when logging in for the first time.");
		resourceCountry2Public.setPublicResource(true);
		resourceCountry2Public.setScopeFk(root.getPk());
		resourceCountry2Public.setUserFk(dataManager.getPk());
		resourceService.createResource(resourceCountry2Public, dataManager, context);

		final Resource resourceCenter1 = new Resource();
		resourceCenter1.setCategoryId("NEWS");
		resourceCenter1.setTitle("Putting study on hold due to current situation");
		resourceCenter1.setDescription("bla");
		resourceCenter1.setPublicResource(false);
		resourceCenter1.setScopeFk(at01.getPk());
		resourceCenter1.setUserFk(dataEntryMaster.getPk());
		resourceService.createResource(resourceCenter1, dataManager, context);

		final Resource resourceCenter2Public = new Resource();
		resourceCenter2Public.setCategoryId("NEWSLETTERS");
		resourceCenter2Public.setTitle("Situation 03/20");
		resourceCenter2Public.setDescription("A brief summary of this trimester.");
		resourceCenter2Public.setPublicResource(false);
		resourceCenter2Public.setScopeFk(at02.getPk());
		resourceCenter2Public.setUserFk(dataManager.getPk());
		resourceService.createResource(resourceCenter2Public, dataManager, context);

		final Resource resourceCenter3 = new Resource();
		resourceCenter3.setCategoryId("NEWSLETTERS");
		resourceCenter3.setTitle("Situation 06/20");
		resourceCenter3.setDescription("A brief summary of this trimester.");
		resourceCenter3.setPublicResource(false);
		resourceCenter3.setScopeFk(fr02.getPk());
		resourceCenter3.setUserFk(dataManager.getPk());
		resourceService.createResource(resourceCenter3, dataManager, context);
	}
}
