package ch.rodano.core.services.bll.export.dct;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.export.DocumentHelper;
import ch.rodano.configuration.model.export.Selection;
import ch.rodano.configuration.model.export.SelectionNode;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.utils.DisplayableUtils;
import ch.rodano.configuration.utils.ExportableUtils;
import ch.rodano.core.configuration.core.Configurator;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.AuditTrail;
import ch.rodano.core.model.audit.models.DatasetAuditTrail;
import ch.rodano.core.model.audit.models.EventAuditTrail;
import ch.rodano.core.model.audit.models.FieldAuditTrail;
import ch.rodano.core.model.audit.models.FormAuditTrail;
import ch.rodano.core.model.audit.models.ScopeAuditTrail;
import ch.rodano.core.model.audit.models.WorkflowStatusAuditTrail;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.event.Timeframe;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.field.FieldRecord;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.layout.builder.PageStateService;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.workflow.WorkflowStatusRecord;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.export.fop.FOPService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.form.FormService;
import ch.rodano.core.services.bll.scope.ScopeRelationService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.dao.dataset.DatasetDAOService;
import ch.rodano.core.services.dao.event.EventDAOService;
import ch.rodano.core.services.dao.field.FieldDAOService;
import ch.rodano.core.services.dao.form.FormDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.services.dao.workflow.WorkflowStatusDAOService;

@Profile("!migration")
@Service
public class CRFDocumentationServiceImpl implements CRFDocumentationService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static final String CRF_BLANK_TEMPLATE = "/documentation/xsl/crf_blank.xsl";
	private static final String CRF_ARCHIVE_TEMPLATE = "/documentation/xsl/crf_archive.xsl";
	private static final String CRF_TEMPORARY_ARCHIVE_FOLDER = "crf_archive";

	private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);
	private static final String ZIP_EXTENSION = ".zip";
	private static final Comparator<AuditTrail> TRAIL_COMPARATOR = Comparator.comparing(AuditTrail::getAuditDatetime).reversed();

	private final StudyService studyService;
	private final FOPService fopService;
	private final ScopeRelationService scopeRelationService;
	private final ScopeDAOService scopeDAOService;
	private final EventService eventService;
	private final EventDAOService eventDAOService;
	private final DatasetDAOService datasetDAOService;
	private final FormService formService;
	private final FormDAOService formDAOService;
	private final FieldService fieldService;
	private final FieldDAOService fieldDAOService;
	private final WorkflowStatusService workflowStatusService;
	private final WorkflowStatusDAOService workflowStatusDAOService;
	private final ActorService actorService;
	private final PageStateService pageStateService;
	private final TaskExecutor taskExecutor;

	private final AtomicBoolean generationInProgress;
	private final File crfArchiveFolder;
	private String generationId;

	public CRFDocumentationServiceImpl(
		final StudyService studyService,
		final FOPService fopService,
		final ScopeRelationService scopeRelationService,
		final ScopeDAOService scopeDAOService,
		final EventService eventService,
		final EventDAOService eventDAOService,
		final DatasetDAOService datasetDAOService,
		final FormService formService,
		final FormDAOService formDAOService,
		final FieldService fieldService,
		final FieldDAOService fieldDAOService,
		final WorkflowStatusService workflowStatusService,
		final WorkflowStatusDAOService workflowStatusDAOService,
		final ActorService actorService,
		final PageStateService pageStateService,
		final TaskExecutor taskExecutor,
		final Configurator configurator
	) {
		this.studyService = studyService;
		this.fopService = fopService;
		this.scopeRelationService = scopeRelationService;
		this.scopeDAOService = scopeDAOService;
		this.eventService = eventService;
		this.eventDAOService = eventDAOService;
		this.datasetDAOService = datasetDAOService;
		this.formService = formService;
		this.formDAOService = formDAOService;
		this.fieldService = fieldService;
		this.fieldDAOService = fieldDAOService;
		this.workflowStatusService = workflowStatusService;
		this.workflowStatusDAOService = workflowStatusDAOService;
		this.actorService = actorService;
		this.pageStateService = pageStateService;
		this.taskExecutor = taskExecutor;

		this.crfArchiveFolder = new File(configurator.getTempFolder(), CRF_TEMPORARY_ARCHIVE_FOLDER);
		this.generationInProgress = new AtomicBoolean(false);
	}

	public <T extends AuditTrail> Element generateTrailsElement(final Document doc, final NavigableSet<T> auditTrails) {
		final var trailsE = doc.createElement("trails");

		for(final var auditTrail : auditTrails) {
			final var trailE = doc.createElement("trail");
			trailsE.appendChild(trailE);

			final var children = new HashMap<String, String>();
			children.put("date", auditTrail.getAuditDatetime().format(DATETIME_FORMATTER));
			children.put("user", auditTrail.getAuditActor());
			children.put("message", auditTrail.getAuditContext());

			if(auditTrail instanceof final WorkflowStatusAuditTrail workflowStatusAuditTrail) {
				children.put("status", workflowStatusAuditTrail.getStateId());
			}
			DocumentHelper.appendSimpleChildren(trailE, children);
		}

		return trailsE;
	}

	public Element generateFieldTrailsElement(
		final Document doc,
		final Scope scope,
		final Optional<Event> event,
		final Dataset dataset,
		final Field field,
		final Optional<ZonedDateTime> date,
		final String... languages
	) {
		final var trailsE = doc.createElement("trails");

		final Map<Long, String> workflowIds = new HashMap<>();

		final var trails = new ArrayList<AuditTrail>();

		//merge field and workflow status trails
		final var auditTrails = fieldDAOService.getAuditTrailsForProperty(field, Optional.empty(), FieldRecord::getValue);
		final var filteredTrails = auditTrails.descendingSet().stream()
			.filter(t -> date.isEmpty() || t.getAuditDatetime().isBefore(date.get()) || t.getAuditDatetime().equals(date.get()))
			.toList();
		trails.addAll(filteredTrails);

		for(final var ws : workflowStatusDAOService.getWorkflowStatusesByFieldPk(field.getPk())) {
			workflowIds.put(ws.getPk(), ws.getId());
			final var wsTrails = workflowStatusDAOService.getAuditTrailsForProperty(ws, Optional.empty(), WorkflowStatusRecord::getStateId);
			final var filteredWSTrails = wsTrails.descendingSet().stream()
				.filter(t -> date.isEmpty() || t.getAuditDatetime().isBefore(date.get()) || t.getAuditDatetime().equals(date.get()))
				.toList();
			trails.addAll(filteredWSTrails);
		}
		trails.sort(TRAIL_COMPARATOR);

		//show trails
		for(final var trail : trails) {
			final var trailE = doc.createElement("trail");

			final var trailDateE = doc.createElement("date");
			trailDateE.setTextContent(trail.getAuditDatetime().format(DATETIME_FORMATTER));
			trailE.appendChild(trailDateE);

			final var trailFromE = doc.createElement("user");
			trailFromE.setTextContent(StringUtils.defaultString(trail.getAuditActor()));
			trailE.appendChild(trailFromE);

			//trail on field
			if(trail instanceof final FieldAuditTrail fieldTrail) {
				//retrieve audit trail value
				final var stringValue = fieldService.valueToLabel(scope, event, dataset, field, fieldTrail.getValue(), languages);

				final var trailValueE = doc.createElement("value");
				trailValueE.setTextContent(stringValue);
				trailE.appendChild(trailValueE);
			}
			//trail on a workflow status attached to the field
			else {
				final var trailWorkflowE = doc.createElement("workflow");
				trailWorkflowE.setTextContent(workflowIds.get(trail.getAuditObjectFk()));
				trailE.appendChild(trailWorkflowE);

				final var trailStatusE = doc.createElement("state");
				trailStatusE.setTextContent(((WorkflowStatusAuditTrail) trail).getStateId());
				trailE.appendChild(trailStatusE);
			}

			final var commentE = doc.createElement("message");
			commentE.setTextContent(StringUtils.defaultString(trail.getAuditContext()));
			trailE.appendChild(commentE);

			trailsE.appendChild(trailE);
		}

		return trailsE;
	}

	/**
	 * Get the export with all data for the xml
	 *
	 * @param scope     The scope to export
	 * @param parent    The parent scope of the scope to export
	 * @param date      An optional containing the maximum date for the data
	 * @param languages The languages to export the data to
	 * @return A document containing all information
	 */
	private Document getExportWithDataForXml(final Scope scope, final Scope parent, final Optional<ZonedDateTime> date, final String... languages) {
		// Make sure that the scope if a leaf scope
		if(!scope.getScopeModel().isLeaf()) {
			return null;
		}

		final var study = studyService.getStudy();

		final var doc = DocumentHelper.createDocument();

		//build timeframe matching the stop date
		final var timeframe = new Timeframe(Optional.empty(), date);

		//build the lists of interesting audit trail properties for each entity
		final List<Function<ScopeAuditTrail, Object>> scopeProperties = Arrays.asList(ScopeAuditTrail::getDeleted, ScopeAuditTrail::getLocked);
		final List<Function<EventAuditTrail, Object>> visitProperties = Arrays.asList(EventAuditTrail::getDeleted, EventAuditTrail::getLocked);
		final List<Function<FormAuditTrail, Object>> formProperties = Arrays.asList(FormAuditTrail::getDeleted);
		final List<Function<DatasetAuditTrail, Object>> datasetProperties = Arrays.asList(DatasetAuditTrail::getDeleted);
		final List<Function<WorkflowStatusAuditTrail, Object>> workflowStatusProperties = Arrays.asList(WorkflowStatusAuditTrail::getStateId);

		final var studyE = ExportableUtils.getExportForXml(doc, study, languages);
		doc.appendChild(studyE);

		//check if scope has been selected
		final var studySelections = study.getSelections();
		final var scopeModelSelection = SelectionNode.getSelection(studySelections, Entity.SCOPE_MODEL, scope.getScopeModelId());
		if(scopeModelSelection.isEmpty()) {
			return null;
		}

		final var scopeE = doc.createElement("scope");
		studyE.appendChild(scopeE);
		scopeE.setAttribute("scopeModelId", scope.getScopeModelId());
		scopeE.setAttribute("scopeModelLabel", scope.getScopeModel().getDefaultLocalizedShortname());
		scopeE.setAttribute("id", scope.getId());
		scopeE.setAttribute("code", scope.getCode());
		scopeE.setAttribute("deleted", Boolean.toString(scope.getDeleted()));
		scopeE.setAttribute("shortname", scope.getShortname());
		scopeE.setAttribute("longname", scope.getLongname());
		scopeE.setAttribute("codeandshortname", scope.getCodeAndShortname());
		final var parentScopeModel = parent.getScopeModel();
		scopeE.setAttribute("parentScopeModelLabel", parentScopeModel.getDefaultLocalizedShortname());
		scopeE.setAttribute("parentCode", parent.getCode());
		scopeE.setAttribute("parentShortname", parent.getShortname());
		scopeE.setAttribute("parentLongname", parent.getLongname());

		scopeE.appendChild(generateTrailsElement(doc, scopeDAOService.getAuditTrailsForProperties(scope, Optional.of(timeframe), scopeProperties)));

		//retrieve all visits that occur before the date parameter (in case of a patient transfer)
		final var events = eventService.getAllIncludingRemoved(scope)
			.stream()
			.filter(e -> date.isEmpty() || e.getDateOrExpectedDate().isBefore(date.get()) || e.getDateOrExpectedDate().equals(date.get()))
			.sorted()
			.toList();

		for(final var event : events) {
			//check if event has been selected
			final var eventModelSelection = scopeModelSelection.get().getSelection(Entity.EVENT_MODEL, event.getEventModelId());
			if(eventModelSelection.isEmpty()) {
				continue;
			}

			final var eventE = doc.createElement("event");
			eventE.setAttribute("id", event.getId());
			eventE.setAttribute("eventModelId", event.getEventModelId());
			eventE.setAttribute("deleted", Boolean.toString(event.getDeleted()));
			eventE.setAttribute("shortname", event.getEventModel().getLocalizedShortname(languages));
			eventE.setAttribute("label", eventService.getLabel(scope, event, languages));
			eventE.setAttribute("expected", Boolean.toString(event.isExpected()));

			eventE.appendChild(generateTrailsElement(doc, eventDAOService.getAuditTrailsForProperties(event, Optional.of(timeframe), visitProperties)));
			scopeE.appendChild(eventE);

			final var forms = new ArrayList<Form>(formService.getAllIncludingRemoved(event));
			forms.sort(Form.getEventModelComparator(event.getEventModel()));

			for(final var form : forms) {
				//check if form has been selected
				final var formSelection = eventModelSelection.get().getSelection(Entity.FORM_MODEL, form.getFormModelId());
				if(formSelection.isEmpty()) {
					continue;
				}

				//build form representation
				final var pageState = pageStateService.createPageState(scope, Optional.of(event), form);
				//initialize page state with date of transfer
				pageStateService.initPage(pageState, date, false);

				final var formE = doc.createElement("form");
				formE.setAttribute("id", form.getId());
				formE.setAttribute("formModelId", form.getFormModelId());
				formE.setAttribute("deleted", Boolean.toString(form.getDeleted()));
				formE.setAttribute("shortname", form.getFormModel().getLocalizedShortname(languages));
				formE.appendChild(generateTrailsElement(doc, formDAOService.getAuditTrailsForProperties(form, Optional.of(timeframe), formProperties)));
				eventE.appendChild(formE);

				for(final var layoutGroupState : pageState.getLayoutGroupStates()) {
					final var layout = layoutGroupState.getLayout();

					//check if layout has been selected
					final var layoutSelection = formSelection.get().getSelection(Entity.LAYOUT, layout.getId());
					if(layoutSelection.isEmpty()) {
						continue;
					}

					final var layoutGroupE = doc.createElement("layoutGroup");
					layoutGroupE.setAttribute("type", layout.getType().name());
					formE.appendChild(layoutGroupE);

					final var repeatable = layout.getType().isRepeatable();
					//add model for repeatable layout and prepare layout id
					if(repeatable) {
						for(final var line : layout.getLines()) {
							final var lineE = doc.createElement("line");
							layoutGroupE.appendChild(lineE);

							for(final var cell : line.getCells()) {
								lineE.appendChild(ExportableUtils.getExportForXml(doc, cell, languages));
							}
						}
					}

					final var layoutGroupLayoutsE = doc.createElement("layouts");
					layoutGroupE.appendChild(layoutGroupLayoutsE);

					int index = 0;
					for(final var layoutState : layoutGroupState.getLayoutStates(date)) {
						final var layoutE = doc.createElement("layout");
						//add more attributes for repeatable layouts
						if(repeatable) {
							final var dataset = layoutState.getReferenceDataset();
							layoutE.setAttribute("documentId", dataset.getDatasetModelId());
							layoutE.setAttribute("index", Integer.toString(++index));
							layoutE.setAttribute("deleted", Boolean.toString(dataset.getDeleted()));
							layoutE.appendChild(generateTrailsElement(doc, datasetDAOService.getAuditTrailsForProperties(dataset, Optional.of(timeframe), datasetProperties)));
						}
						layoutGroupLayoutsE.appendChild(layoutE);

						final var columnsElement = doc.createElement("columns");
						layoutE.appendChild(columnsElement);
						for(final var column : layout.getColumns()) {
							columnsElement.appendChild(ExportableUtils.getExportForXml(doc, column, languages));
						}

						final var linesElement = doc.createElement("lines");
						layoutE.appendChild(linesElement);
						for(final var line : layout.getLines()) {
							final var lineE = doc.createElement("line");
							linesElement.appendChild(lineE);

							for(final var cell : line.getCells()) {

								final var cellE = ExportableUtils.getExportForXml(doc, cell, languages);
								lineE.appendChild(cellE);

								//retrieve cell state
								final var cellState = layoutState.getCellState(cell.getId());
								//cell state may not exist in layout if it is hidden by a constraint
								if(cellState.isPresent()) {
									cellE.setAttribute("visible", Boolean.toString(cellState.get().isVisible()));

									if(cell.hasFieldModel() && cellState.get().isVisible()) {
										final var dataset = cellState.get().getDataset();
										final var field = cellState.get().getField();
										final var fieldModel = field.getFieldModel();

										final var fieldE = doc.createElement("field");
										fieldE.setAttribute("fieldModelId", fieldModel.getId());
										fieldE.setAttribute("type", fieldModel.getType().name());
										fieldE.setAttribute("label", fieldModel.getLocalizedLabel(languages));
										fieldE.setAttribute("inlineHelp", fieldModel.getInlineHelp());
										fieldE.setAttribute("value", fieldService.getValueLabel(scope, Optional.of(event), dataset, field, date, languages));
										fieldE.appendChild(generateFieldTrailsElement(doc, scope, Optional.of(event), dataset, field, date, languages));
										cellE.appendChild(fieldE);
									}
								}
							}
						}
					}
				}
			}
		}

		//add signature workflow
		final var workflowId = "SIGNATURE";
		final var workflow = study.getWorkflow(workflowId);
		final var ws = workflowStatusService.getMostRecent(scope, workflow);

		if(ws.isPresent()) {
			final var workflowE = doc.createElement("workflow");
			workflowE.setAttribute("workflowId", workflowId);

			final var workflowAuditTrails = workflowStatusDAOService.getAuditTrailsForProperties(ws.get(), Optional.of(timeframe), workflowStatusProperties).descendingSet();
			workflowE.appendChild(generateTrailsElement(doc, workflowAuditTrails));

			scopeE.appendChild(workflowE);

			//signature workflow should have only action to sign
			final var action = workflow.getActions().iterator().next();

			final var signatureText = DisplayableUtils.getLocalizedMap(action.getRequiredSignatureText(), languages);
			if(!StringUtils.isBlank(signatureText)) {
				workflowE.appendChild(DocumentHelper.textAsNode(doc, "signatureText", signatureText));
			}
		}
		else {
			logger.error(String.format("Workflow %s not initialized", workflowId));
		}

		doc.getDocumentElement().normalize();
		return doc;
	}

	@Override
	public void generateCRFArchive(final Actor actor, final Scope scope, final boolean withAuditTrails, final OutputStream outputStream) {
		generateCRFArchive(actor, scope, scopeRelationService.getDefaultParent(scope), Optional.empty(), withAuditTrails, outputStream);
	}

	/**
	 * Generate the CRF archive
	 *
	 * @param actor           The actor performing the export
	 * @param scope           The scope to export
	 * @param parent          The parent scope of the scope to export
	 * @param date            An optional containing the stop date of the scope or an empty optional if the stop date is null
	 * @param withAuditTrails Include the audit trails in the archive
	 * @param outputStream    The output stream to write the report in
	 */
	private void generateCRFArchive(
		final Actor actor,
		final Scope scope,
		final Scope parent,
		final Optional<ZonedDateTime> date,
		final boolean withAuditTrails,
		final OutputStream outputStream
	) {
		final var archiveData = getExportWithDataForXml(scope, parent, date, actorService.getLanguages(actor));

		final var parameters = Map.of(
			"audit_trail", Boolean.toString(withAuditTrails),
			"generation_time", DATETIME_FORMATTER.format(ZonedDateTime.now())
		);

		try(var xslTemplate = getClass().getResourceAsStream(CRF_ARCHIVE_TEMPLATE)) {
			fopService.writePDF(outputStream, archiveData, xslTemplate, parameters);
		}
		catch(final IOException e) {
			throw new RuntimeException("Unable to find archive CRF template in the JAR file", e);
		}
	}

	/**
	 * Generate all the CRF archives files for the descendants of the given scope
	 *
	 * @param scope      The scope to export
	 * @param parent     The parent scope of the scope to export
	 * @param parameters The parameters of the export
	 * @param folder     The folder where the report should be created
	 */
	private void generateCRFArchives(
		final Actor actor,
		final ScopeModel scopeModel,
		final Scope scope,
		final Optional<Scope> parent,
		final Optional<ZonedDateTime> date,
		final boolean withAuditTrails,
		final File folder
	) {
		//target scope model has not been reached
		if(!scope.getScopeModel().equals(scopeModel)) {
			// Go through all descendants
			for(final var scopeRelation : scopeRelationService.getChildRelations(scope)) {
				generateCRFArchives(
					actor,
					scopeModel,
					scopeRelationService.getChild(scopeRelation),
					Optional.of(scope),
					Optional.ofNullable(scopeRelation.getEndDate()),
					withAuditTrails,
					new File(folder, scope.getCode())
				);
			}
		}
		else {
			// Make sure the directory is created
			folder.mkdirs();

			final var outputFile = new File(folder, generateCRFArchiveFilename(studyService.getStudy(), scope));
			try(OutputStream output = new FileOutputStream(outputFile)) {
				generateCRFArchive(actor, scope, parent.orElse(scopeRelationService.getDefaultParent(scope)), date, withAuditTrails, output);
			}
			catch(final IOException e) {
				logger.error("The CRF archive file for the scope {} cannot be written: {}", scope.getCode(), e.getLocalizedMessage());
			}
		}
	}

	@Override
	public void generateCRFBlank(final Actor actor, final ScopeModel scopeModel, final boolean annotated, final OutputStream outputStream) {
		final org.w3c.dom.Document doc = Selection.getExportForXml(studyService.getStudy(), actorService.getLanguages(actor));
		final var parameters = Map.of(
			"scopeModelId", scopeModel.getId(),
			"annotated", Boolean.toString(annotated)
		);
		try(final var xslTemplate = getClass().getResourceAsStream(CRF_BLANK_TEMPLATE)) {
			fopService.writePDF(outputStream, doc, xslTemplate, parameters);
		}
		catch(final IOException e) {
			throw new RuntimeException("Unable to find blank CRF template in the JAR file", e);
		}
	}

	@Override
	public String generateCRFArchive(final Actor actor, final ScopeModel scopeModel, final List<Scope> scopes, final boolean withAuditTrails) {
		// Queue and return an uid if no report is currently being generated and set generation in progress
		if(generationInProgress.getAndSet(true)) {
			logger.info("A CRF archive task is already running");
			return generationId;
		}

		try {
			cleanOldCRFArchives();
		}
		catch(final IOException e) {
			generationInProgress.set(false);
			throw new RuntimeException("Unable to clean old CRF archives", e);
		}

		// Set the id of the generation
		generationId = UUID.randomUUID().toString();

		taskExecutor.execute(() -> {
			logger.info(
				"Creating CRF archive for root scope [{}] with id {} into {} ",
				scopes.stream().map(Scope::getCode).collect(Collectors.joining(", ")),
				generationId,
				crfArchiveFolder
			);

			try {
				for(final var scope : scopes) {
					generateCRFArchives(actor, scopeModel, scope, Optional.empty(), Optional.empty(), withAuditTrails, crfArchiveFolder);
				}
				logger.info("CRF archive with id {} created successfully", generationId);
			}
			catch(final Exception e) {
				logger.error("Unable to create CRF archive", e);
			}
			finally {
				generationId = null;
				generationInProgress.set(false);
			}
		});

		return generationId;
	}

	@Override
	public String generateCRFArchiveFilename(final Study study, final Scope scope) {
		return String.format("%s_%s.pdf", study.getDefaultLocalizedShortname(), scope.getCode());
	}

	@Override
	public CRFDocumentationGenerationStatus getCRFArchiveGenerationStatus() {
		if(generationInProgress.get()) {
			return CRFDocumentationGenerationStatus.IN_PROGRESS;
		}
		if(crfArchiveFolder.exists()) {
			return CRFDocumentationGenerationStatus.COMPLETED;
		}
		return CRFDocumentationGenerationStatus.NOT_STARTED;
	}

	@Override
	public void streamCRFArchive(final OutputStream os) throws IOException {
		// Generation in progress return
		if(generationInProgress.get()) {
			logger.info("A CRF archive task is running");
			return;
		}

		// Check that something has been generated
		if(!crfArchiveFolder.exists()) {
			logger.info("No CRF archive is available");
			return;
		}

		logger.info("Generate the zip file of the last generated CRF archive task");

		// Stream into a zip
		final var zip = new ZipOutputStream(os);

		// If more than one we do zip-ception :D
		final var files = crfArchiveFolder.listFiles();
		if(files.length > 1) {
			for(final var file : files) {
				if(!file.isDirectory()) {
					addToZipFile(zip, file, Optional.of(Path.of(file.getName())));
					continue;
				}

				// Add the child zip to the main one
				zip.putNextEntry(new ZipEntry(file.getName() + ZIP_EXTENSION));

				// Create child zip
				final var childZip = new ZipOutputStream(zip);

				// Fill the child zip
				addToZipFile(childZip, file, Optional.empty());

				// Write the child zip without closing the sub-output stream
				childZip.finish();

				// Close the entry of the parent
				zip.closeEntry();
			}
		}
		else {
			addToZipFile(zip, crfArchiveFolder, Optional.empty());
		}

		// Write the zip without closing the sub-output stream
		zip.finish();
	}

	/**
	 * Recursively add all sub-folders and files into the zip file
	 *
	 * @param zip  The zip file
	 * @param file The file to add to the zip
	 * @param zipPath The path of the file in the zip file
	 * @throws IOException Thrown if an error occurred while creating the zip file
	 */
	private void addToZipFile(final ZipOutputStream zip, final File file, final Optional<Path> zipPath) throws IOException {
		//directory
		if(file.isDirectory()) {
			for(final var subFile : file.listFiles()) {
				final var subPath = zipPath.map(p -> p.resolve(subFile.getName())).orElse(Path.of(subFile.getName()));
				addToZipFile(zip, subFile, Optional.of(subPath));
			}
		}
		//file
		else {
			zip.putNextEntry(new ZipEntry(zipPath.get().toString()));

			// Write the file into the zip
			try(InputStream is = new FileInputStream(file)) {
				is.transferTo(zip);
				zip.closeEntry();
			}
		}
	}

	/**
	 * Clean old CRF archives
	 *
	 * @throws IOException Thrown if the directory cannot be recursively deleted
	 */
	private void cleanOldCRFArchives() throws IOException {
		// Delete the folder and its subtree if it exists
		if(crfArchiveFolder.exists()) {
			FileUtils.deleteDirectory(crfArchiveFolder);
		}
		crfArchiveFolder.mkdirs();
	}

	@Override
	public String getCRFArchiveFilename() {
		// Generation in progress return
		if(generationInProgress.get()) {
			throw new UnsupportedOperationException("A CRF archive task is running");
		}

		// Check that something has been generated
		if(!crfArchiveFolder.exists()) {
			throw new UnsupportedOperationException("No CRF archive is available");
		}

		final var files = crfArchiveFolder.listFiles();
		if(files.length > 1) {
			return CRF_TEMPORARY_ARCHIVE_FOLDER;
		}

		//the file can be a folder or a file but FilenameUtils::getBaseName works with both
		return FilenameUtils.getBaseName(files[0].getName()) + ZIP_EXTENSION;
	}
}
