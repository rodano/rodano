package ch.rodano.core.services.bll.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.rodano.api.controller.form.exception.DatasetSubmissionException;
import ch.rodano.api.dataset.DatasetCreationDTO;
import ch.rodano.api.dataset.DatasetRestorationDTO;
import ch.rodano.api.dataset.DatasetSubmissionDTO;
import ch.rodano.api.dataset.DatasetUpdateDTO;
import ch.rodano.api.dto.layout.FieldPeer;
import ch.rodano.api.exception.http.ForbiddenArgumentException;
import ch.rodano.api.field.FieldUpdateDTO;
import ch.rodano.api.form.BlockingErrorDTO;
import ch.rodano.api.utils.URLConsistencyUtils;
import ch.rodano.configuration.exceptions.NoNodeException;
import ch.rodano.configuration.exceptions.NoRespectForConfigurationException;
import ch.rodano.configuration.model.rights.RightAssignable;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.exception.UnauthorizedException;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.field.ValidationService;
import ch.rodano.core.services.bll.file.FileService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.dataset.DatasetDAOService;
import ch.rodano.core.services.plugin.validator.exception.BadlyFormattedValue;
import ch.rodano.core.services.plugin.validator.exception.InvalidValueException;
import ch.rodano.core.services.plugin.validator.exception.ValidatorException;
import ch.rodano.core.utils.ACL;

@Service
public class DatasetSubmissionServiceImpl implements DatasetSubmissionService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final StudyService studyService;
	private final DatasetService datasetService;
	private final DatasetDAOService datasetDAOService;
	private final FieldService fieldService;
	private final FileService fileService;
	private final ValidationService validationService;

	public DatasetSubmissionServiceImpl(
		final StudyService studyService,
		final DatasetService datasetService,
		final DatasetDAOService datasetDAOService,
		final FieldService fieldService,
		final FileService fileService,
		final ValidationService validationService
	) {
		this.studyService = studyService;
		this.datasetService = datasetService;
		this.datasetDAOService = datasetDAOService;
		this.fieldService = fieldService;
		this.fileService = fileService;
		this.validationService = validationService;
	}

	@Override
	public Collection<Dataset> submit(
		final ACL acl,
		final Scope scope,
		final Optional<Event> event,
		final DatasetSubmissionDTO datasetSubmissionDTO,
		final DatabaseActionContext context,
		final Optional<String> rationale
	) throws DatasetSubmissionException {
		//keep track of all datasets that are updated (that includes saved datasets and deleted datasets)
		final var updatedDatasets = new ArrayList<Dataset>();

		//remove datasets
		for(final Entry<Long, String> removedDataset : datasetSubmissionDTO.getRemovedDatasets().entrySet()) {
			final var dataset = datasetDAOService.getDatasetByPk(removedDataset.getKey());
			updatedDatasets.add(dataset);
			final var datasetModel = dataset.getDatasetModel();
			if(!datasetModel.isMultiple()) {
				throw new ForbiddenArgumentException(String.format("Dataset %d cannot be removed because it's not a multiple dataset", dataset.getPk()));
			}
			datasetService.delete(scope, event, dataset, context, removedDataset.getValue());
		}

		//restore datasets
		for(final var restoredDataset : datasetSubmissionDTO.getRestoredDatasets()) {
			final var dataset = datasetDAOService.getDatasetByPk(restoredDataset.dataset().getPk());
			final var datasetModel = dataset.getDatasetModel();
			if(!datasetModel.isMultiple()) {
				throw new ForbiddenArgumentException(String.format("Dataset %d cannot be restored because it's not a multiple dataset", dataset.getPk()));
			}
			datasetService.restore(scope, event, dataset, context, restoredDataset.rationale());
		}

		//new multiple datasets must be created in the database now
		final var newDatasetDTOs = createMissingDatasets(scope, event, datasetSubmissionDTO.getNewDatasets(), context, rationale.orElse("Save datasets"));

		final List<DatasetUpdateDTO> datasetDTOsToSave = new ArrayList<>();
		datasetDTOsToSave.addAll(newDatasetDTOs);
		datasetDTOsToSave.addAll(datasetSubmissionDTO.getRestoredDatasets().stream().map(DatasetRestorationDTO::dataset).toList());
		datasetDTOsToSave.addAll(datasetSubmissionDTO.getUpdatedDatasets());

		updatedDatasets.addAll(saveDatasets(acl, scope, event, datasetDTOsToSave, context, rationale));

		//return all updated datasets
		return updatedDatasets;
	}

	@Override
	public Collection<Dataset> createAndSaveDatasets(
		final ACL acl,
		final Scope scope,
		final Optional<Event> event,
		final Collection<DatasetCreationDTO> datasetCreationDTOs,
		final DatabaseActionContext context,
		final Optional<String> rationale
	) throws DatasetSubmissionException {

		final var datasetUpdateDTOs = createMissingDatasets(scope, event, datasetCreationDTOs, context, rationale.orElse("Save datasets"));
		return saveDatasets(acl, scope, event, datasetUpdateDTOs, context, rationale);
	}

	//never forget:
	//- it's required to save all datasets together
	//  the goal is that all fields are set before plugins are calculated and the validation is performed
	//- that an event may be provided even if scope datasets are submitted
	//  the problem is that this method is used to save multiple datasets at once
	@Override
	public Collection<Dataset> saveDatasets(
		final ACL acl,
		final Scope scope,
		final Optional<Event> event,
		final Collection<DatasetUpdateDTO> datasetDTOs,
		final DatabaseActionContext context,
		final Optional<String> rationale
	) throws DatasetSubmissionException {
		//nothing to do if no DTOs are submitted
		if(datasetDTOs.isEmpty()) {
			return Collections.emptyList();
		}

		//retrieve all datasets that match submitted dataset dtos
		final var datasets = datasetDAOService.getDatasetByPks(datasetDTOs.stream().map(DatasetUpdateDTO::getPk).toList());

		//check dataset consistency to avoid forged datasetPks by the client
		for(final Dataset dataset : datasets) {
			//do not use the provided event as the context for the check of consistency
			//for scope datasets, no event should be provided
			final var datasetEvent = dataset.getEventFk() != null ? event : Optional.<Event> empty();
			URLConsistencyUtils.checkConsistency(scope, datasetEvent, dataset);
		}

		//check WRITE right on all datasets that match submitted dataset dtos
		//the goal is to avoid forged datasetModelIds by the client
		//do not check rights on scope model or event group: they are not required to save data
		final List<RightAssignable<?>> unauthorizedDatasetModels = datasets.stream()
			.map(Dataset::getDatasetModel)
			.filter(d -> !acl.hasRight(d, Rights.WRITE))
			.collect(Collectors.toList());
		if(!unauthorizedDatasetModels.isEmpty()) {
			throw UnauthorizedException.getInstance(unauthorizedDatasetModels, Rights.WRITE);
		}

		//retrieve all submitted fields and transform them in field peers
		final Map<Long, Dataset> datasetByPk = datasets.stream().collect(Collectors.toMap(Dataset::getPk, Function.identity()));
		final var submittedFields = datasetDTOs.stream()
			.flatMap(d -> generateFieldPeers(datasetByPk.get(d.getPk()), d.getFields()).stream())
			.toList();

		//field read only fields
		final var fieldPeers = submittedFields.stream()
			.filter(f -> !f.field().getFieldModel().isReadOnly())
			.toList();

		//separate visible values from hidden values (according to visibility criteria)
		final var hiddenFieldPeers = fieldPeers.stream().filter(p -> p.fieldDTO().isReset()).toList();
		final var visibleFieldPeers = fieldPeers.stream().filter(p -> !p.fieldDTO().isReset()).toList();

		//format values (null values become "")
		visibleFieldPeers.stream().map(FieldPeer::fieldDTO).filter(f -> f.getValue() == null).forEach(f -> f.setValue(""));

		//link submitted files to fields
		//when files are uploaded, they are attached only to a scope and/or a event
		//at that time, it's not possible to link them to a dataset and a field because in the case of multiple dataset, those don't exist in the database (hence there is no datasetFk and fieldFk to set)
		//now that datasets and fields have been created in the database, it's possible to attach the uploaded files to their respective files
		//this must be done only for new files
		visibleFieldPeers.stream().filter(f -> f.fieldDTO().getFilePk() != null).forEach(fieldPeer -> {
			final var file = fileService.getFileByPk(fieldPeer.fieldDTO().getFilePk());
			if(!file.isSubmitted()) {
				fileService.linkFileToField(file, fieldPeer.field(), context, "Link file to field properly");
			}
		});

		//HERE WE ARE! serious stuff happens from now on
		//at this point submitted fields are set in the real object graph to make rules work (rules rely on the whole object graph to do the validation)

		//first of all reset hidden fields
		logger.info(
			"Resetting {} fields [{}]",
			hiddenFieldPeers.size(),
			hiddenFieldPeers.stream().map(FieldPeer::getId).collect(Collectors.joining(","))
		);

		for(final var hiddenFieldPeer : hiddenFieldPeers) {
			final var field = hiddenFieldPeer.field();
			fieldService.reset(scope, event, hiddenFieldPeer.dataset(), field, context, hiddenFieldPeer.fieldDTO().getRationale());
		}

		//set value from field DTOs to the real fields
		logger.info(
			"Saving {} fields [{}]",
			visibleFieldPeers.size(),
			visibleFieldPeers.stream().map(f -> f.getId() + "=" + f.fieldDTO().getValue()).collect(Collectors.joining(","))
		);
		saveFieldPeers(scope, event, visibleFieldPeers, context, rationale);

		// do only blocking validation
		logger.info("Do blocking validation for saved fields");
		doBlockingValidation(scope, event, visibleFieldPeers);

		// At this point the submitted values will be kept

		//refresh plugins
		logger.info("Calculating plugin values for saved datasets");
		for(final var dataset : datasets) {
			//do not use the provided event as the context for the calculation of plugins
			//for scope datasets, no visits should be provided
			final var datasetEvent = dataset.getEventFk() != null ? event : Optional.<Event> empty();
			// Recalculate the plugin values before doing validation because validation may rely on plugins values
			datasetService.recalculatePluginValues(scope, datasetEvent, dataset, context, "Set calculated value");
		}

		//finally, do the validation (generate the validation workflows on fields)
		logger.info("Validate saved fields");
		for(final var peer : visibleFieldPeers) {
			//do not use the provided event as the context for the validation
			//for scope datasets, no visits should be provided
			final var datasetEvent = peer.dataset().getEventFk() != null ? event : Optional.<Event> empty();
			validationService.validateField(scope, datasetEvent, peer.dataset(), peer.field(), context.toSystemAction(), "Save field");
		}

		return datasets;
	}

	private List<DatasetUpdateDTO> createMissingDatasets(
		final Scope scope,
		final Optional<Event> event,
		final Collection<DatasetCreationDTO> datasetCreationDTOs,
		final DatabaseActionContext context,
		final String rationale
	) {
		final List<DatasetUpdateDTO> datasetUpdateDTOs = new ArrayList<>();
		for(final var datasetCreationDTO : datasetCreationDTOs) {
			final var document = studyService.getStudy().getDatasetModel(datasetCreationDTO.getModelId());
			if(!document.isMultiple()) {
				throw new NoRespectForConfigurationException("A dataset that is not multiple can not be created when submitting data");
			}
			//check that client provided an id
			if(datasetCreationDTO.getId() == null) {
				throw new RuntimeException("Dataset id must be provided to create a dataset");
			}

			final Dataset dataset;
			if(event.isEmpty() || !event.get().getEventModel().getDatasetModelIds().contains(document.getId())) {
				dataset = datasetService.create(scope, document, context, rationale, Optional.of(datasetCreationDTO.getId()));
			}
			else {
				dataset = datasetService.create(scope, event.get(), document, context, rationale, Optional.of(datasetCreationDTO.getId()));
			}

			//create dataset update DTO from the dataset creation DTO
			final var datasetUpdateDTO = new DatasetUpdateDTO();
			datasetUpdateDTO.setFields(datasetCreationDTO.getFields());
			//store the pk of the newly created dataset in the DTO
			datasetUpdateDTO.setPk(dataset.getPk());
			datasetUpdateDTOs.add(datasetUpdateDTO);

			logger.info("Create multiple dataset [{}]", dataset.getDatasetModel().getId());
		}
		return datasetUpdateDTOs;
	}

	/**
	 * Generate field peers
	 *
	 * @param fieldDTOs    The fields from which the peers will be generated
	 * @return Generated value peers
	 */
	private List<FieldPeer> generateFieldPeers(
		final Dataset dataset,
		final List<FieldUpdateDTO> fieldDTOs
	) {
		//retrieve all submitted fields at once
		final var datasetModel = dataset.getDatasetModel();
		final var fieldModels = fieldDTOs.stream()
			.map(FieldUpdateDTO::getModelId)
			.map(datasetModel::getFieldModel)
			.toList();
		final var fieldsByFieldModelId = fieldService.getAll(dataset, fieldModels)
			.stream()
			.collect(Collectors.toMap(Field::getFieldModelId, Function.identity()));

		//transform fields into field peer
		return fieldDTOs.stream()
			.map(fieldDTO -> {
				return new FieldPeer(dataset, fieldsByFieldModelId.get(fieldDTO.getModelId()), fieldDTO);
			})
			.toList();
	}

	/**
	 * Validate and save the given fields
	 *
	 * @param fieldPeers Fields to update
	 * @throws DatasetSubmissionException Thrown if a value is not valid
	 */
	private void saveFieldPeers(final Scope scope, final Optional<Event> event, final List<FieldPeer> fieldPeers, final DatabaseActionContext context, final Optional<String> rationale)
		throws DatasetSubmissionException {
		final List<BlockingErrorDTO> blockingErrorDTOS = new ArrayList<>();

		for(final FieldPeer peer : fieldPeers) {
			final var field = peer.field();
			try {
				fieldService.updateValue(scope, event, peer.dataset(), field, peer.fieldDTO().getValue(), context, rationale.orElse("Save field"));
			}
			catch(final InvalidValueException e) {
				blockingErrorDTOS.add(
					new BlockingErrorDTO(
						peer.dataset().getId(),
						peer.fieldDTO().getModelId(),
						e.getError().getLocalizedMessage(studyService.getStudy().getDefaultLanguage().getId())
					)
				);
			}
			catch(final BadlyFormattedValue e) {
				blockingErrorDTOS.add(
					new BlockingErrorDTO(
						peer.dataset().getId(),
						peer.fieldDTO().getModelId(),
						e.getMessage()
					)
				);
			}
		}

		if(!blockingErrorDTOS.isEmpty()) {
			throw new DatasetSubmissionException(blockingErrorDTOS);
		}
	}

	/**
	 * Perform blocking validation
	 *
	 * @param fieldPeers The values to validate
	 * @throws NoNodeException         Thrown is no node is found
	 * @throws DatasetSubmissionException Thrown if the values validation is not correct
	 */
	private void doBlockingValidation(final Scope scope, final Optional<Event> event, final List<FieldPeer> fieldPeers) throws DatasetSubmissionException {
		// Perform validation from validators
		final List<BlockingErrorDTO> blockingErrorDTOS = new ArrayList<>();
		for(final var fieldPeer : fieldPeers) {
			final var field = fieldPeer.field();
			final var dataset = fieldPeer.dataset();
			try {
				validationService.applyBlockingValidators(scope, event, dataset, field);
			}
			catch(final ValidatorException e) {
				// Only manage blocking validators, ie validators which don't trigger any workflow
				if(e.getValidator().isBlocking()) {
					blockingErrorDTOS.add(
						new BlockingErrorDTO(
							dataset.getId(),
							field.getFieldModel().getId(),
							e.getUserFriendlyErrorMessage(studyService.getStudy().getDefaultLanguageId())
						)
					);
				}
			}
		}

		// Do not go further if there is blocking validators
		if(!blockingErrorDTOS.isEmpty()) {
			// no need to revert values of values peers, all will be discarded at the end of the request
			throw new DatasetSubmissionException(blockingErrorDTOS);
		}
	}
}
