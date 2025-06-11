package ch.rodano.core.database.initializer;

import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.language.Language;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.helpers.ScopeCreatorService;
import ch.rodano.core.helpers.builder.ScopeBuilder;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.audit.AuditActionService;
import ch.rodano.core.services.dao.commons.cache.transaction.TransactionCacheDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.services.plugin.validator.exception.BadlyFormattedValue;
import ch.rodano.core.services.plugin.validator.exception.InvalidValueException;

@Service
public class RandomDataInitializer {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public static final String RATIONALE = "Add random data";

	private final StudyService studyService;
	private final ScopeService scopeService;
	private final ScopeDAOService scopeDAOService;
	private final EventService eventService;
	private final DatasetService datasetService;
	private final FieldService fieldService;
	private final TransactionCacheDAOService transactionCacheDAOService;
	private final ScopeCreatorService scopeCreatorService;
	private final AuditActionService auditActionService;

	public RandomDataInitializer(
		final StudyService studyService,
		final ScopeService scopeService,
		final EventService eventService,
		final DatasetService datasetService,
		final FieldService fieldService,
		final TransactionCacheDAOService transactionCacheDAOService,
		final ScopeCreatorService scopeCreatorService,
		final ScopeDAOService scopeDAOService,
		final AuditActionService auditActionService
	) {
		this.studyService = studyService;
		this.scopeService = scopeService;
		this.scopeDAOService = scopeDAOService;
		this.eventService = eventService;
		this.datasetService = datasetService;
		this.fieldService = fieldService;
		this.transactionCacheDAOService = transactionCacheDAOService;
		this.scopeCreatorService = scopeCreatorService;
		this.auditActionService = auditActionService;
	}

	private ZonedDateTime generateRandomDate(final Optional<ZonedDateTime> minDate, final Optional<ZonedDateTime> maxDate, final Optional<ZonedDateTime> average) {
		if(average.isPresent()) {
			return average.get().minusDays(RandomUtils.nextLong(0, 20) - 10);
		}
		return ZonedDateTime.of(
			RandomUtils.nextInt(minDate.map(ZonedDateTime::getYear).orElse(1920), maxDate.orElse(ZonedDateTime.now()).getYear()),
			RandomUtils.nextInt(1, 13),
			RandomUtils.nextInt(1, 28),
			RandomUtils.nextInt(0, 24),
			RandomUtils.nextInt(0, 60),
			RandomUtils.nextInt(0, 60),
			0,
			ZoneId.of("UTC")
		);
	}

	private String generateValue(final FieldModel fieldModel, final Object averageValue) {
		switch(fieldModel.getType()) {
			case NUMBER:
				final var formatter = fieldModel.getNumberFormatter();
				//adjust minimum and maximum value according to configuration
				//for simplicity, only consider positive number
				double minValue = 0d;
				if(fieldModel.getMinValue() != null) {
					minValue = fieldModel.getMinValue();
				}
				final Double maxValue;
				if(fieldModel.getMaxValue() != null) {
					maxValue = fieldModel.getMaxValue();
				}
				else {
					//adjust maximum value according to length of value and to formatter
					final var maxLength = fieldModel.getMaxLength() != null ? fieldModel.getMaxLength() : 4;
					maxValue = Math.pow(10, Math.min(formatter.getMinimumIntegerDigits(), maxLength)) - 1;
				}

				final Double number = RandomUtils.nextDouble(minValue, maxValue);
				return formatter.format(number);
			case STRING:
				//TODO handle attribute having a matcher
				if(StringUtils.isBlank(fieldModel.getMatcher())) {
					int maxLength = 15;
					if(fieldModel.getMaxLength() != null) {
						maxLength = fieldModel.getMaxLength();
					}

					return RandomStringUtils.random(RandomUtils.nextInt(5, maxLength), true, false);
				}
				return "";
			case TEXTAREA:
				int maxLength = 15;
				if(fieldModel.getMaxLength() != null) {
					maxLength = fieldModel.getMaxLength();
				}
				return RandomStringUtils.random(RandomUtils.nextInt(10, maxLength), true, false);
			case DATE:
			case DATE_SELECT:
				final var minDate = fieldModel.getMinYear() != null ? ZonedDateTime.of(fieldModel.getMinYear(), 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")) : null;
				final var maxDate = fieldModel.getMaxYear() != null ? ZonedDateTime.of(fieldModel.getMaxYear(), 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")) : null;
				final var date = generateRandomDate(
					Optional.ofNullable(minDate),
					Optional.ofNullable(maxDate),
					Optional.ofNullable((ZonedDateTime) averageValue)
				);
				return fieldModel.getDateTimeFormatter().format(date);
			case SELECT:
			case RADIO:
				return fieldModel.getPossibleValues().get(RandomUtils.nextInt(0, fieldModel.getPossibleValues().size())).getId();
			case CHECKBOX:
				return Boolean.toString(RandomUtils.nextBoolean());
			default:
				return "";
		}
	}

	private void fillField(final DatabaseActionContext context, final Scope scope, final Optional<Event> event, final Dataset dataset, final Field field) {
		//check if the field set the event date
		final var fieldModel = field.getFieldModel();
		Object averageValue = null;
		if(fieldModel.getRules().stream().flatMap(r -> r.getActions().stream()).anyMatch(a -> "SET_DATE".equals(a.getActionId()))) {
			averageValue = event.get().getDateOrExpectedDate();
		}
		final var value = generateValue(fieldModel, averageValue);
		try {

			fieldService.updateValue(scope, event, dataset, field, value, context, RATIONALE);
		}
		catch(InvalidValueException | BadlyFormattedValue e) {
			logger.warn(String.format("Unable to fill field %s with the random value %s", field.getId(), value), e);
		}
	}

	private void filldDatasets(final DatabaseActionContext context, final Scope scope, final Optional<Event> event, final List<Dataset> datasets) {
		datasets.forEach(d -> fillDataset(context, scope, event, d));
	}

	private void fillDataset(final DatabaseActionContext context, final Scope scope, final Optional<Event> event, final Dataset dataset) {
		fieldService.getAll(dataset).forEach(f -> fillField(context, scope, event, dataset, f));
	}

	private void fillScope(final DatabaseActionContext context, final Scope scope) {
		filldDatasets(context, scope, Optional.empty(), datasetService.getAllIncludingRemoved(scope));
		//add multiple datasets
		final List<DatasetModel> scopeModelMultipleDatasetModels = scope.getScopeModel().getDatasetModels().stream()
			.filter(DatasetModel::isMultiple)
			.toList();

		for(final var datasetModel : scopeModelMultipleDatasetModels) {
			for(int i = 0; i < RandomUtils.nextInt(0, 4); i++) {
				final Dataset dataset = datasetService.create(scope, datasetModel, context, RATIONALE);
				fillDataset(context, scope, Optional.empty(), dataset);
			}
		}
		//set event date
		final var events = eventService.getAll(scope);
		if(!events.isEmpty()) {
			final var pastStart = ZonedDateTime.now().minusYears(3);
			final var pastStop = ZonedDateTime.now().minusYears(1);
			final var visitDate = generateRandomDate(Optional.of(pastStart), Optional.of(pastStop), Optional.empty());
			eventService.updateDate(scope, events.get(0), visitDate, context, RATIONALE);
		}
		//add multiple datasets in visits
		for(final var event : events) {
			filldDatasets(context, scope, Optional.of(event), datasetService.getAllIncludingRemoved(event));
			//add multiple datasets
			final var eventMultipleDatasetModels = event.getEventModel().getDatasetModels().stream()
				.filter(DatasetModel::isMultiple)
				.toList();

			for(final var datasetModel : eventMultipleDatasetModels) {
				for(int i = 0; i < RandomUtils.nextInt(0, 4); i++) {
					final Dataset dataset = datasetService.create(scope, event, datasetModel, context, RATIONALE);
					fillDataset(context, scope, Optional.of(event), dataset);
				}
			}
		}
	}

	private List<Long> generateScopes(final DatabaseActionContext context, final ZonedDateTime origin, final List<Long> availableParentPks, final ScopeModel scopeModel, final Integer number) {
		//do not keep the list of generated scope in memory
		//only keep their pk
		final List<Long> scopePks = new ArrayList<>();
		final var codePrefix = scopeModel.getId().substring(0, 2);
		final var languages = new Language[] { studyService.getStudy().getDefaultLanguage() };
		final var namePrefix = scopeModel.getLocalizedShortname(languages);
		final DecimalFormat numberFormatter = new DecimalFormat();
		numberFormatter.setMinimumIntegerDigits((int) (Math.log10(number) + 1));
		numberFormatter.setGroupingUsed(false);
		for(var i = 0; i < number; i++) {
			final var code = String.format("%s-%s", codePrefix, numberFormatter.format(i + 1));
			//check if this scope does not already exist
			//do not forget that this script can be used to add random data to an existing database or can be run multiple times
			var scope = scopeDAOService.getScopeByCode(code);
			if(scope == null) {
				logger.info(String.format("Creating %s", code));
				final var parentPk = availableParentPks.get(RandomUtils.nextInt(0, availableParentPks.size()));
				final var parentScope = scopeDAOService.getScopeByPk(parentPk);
				final var name = String.format("%s %s", namePrefix, numberFormatter.format(i + 1));
				scope = scopeCreatorService.createScope(new ScopeBuilder(context, origin).createScope(scopeModel, parentScope, code, name));
				fillScope(context, scope);
			}
			else {
				logger.info(String.format("%s already exists", scope.getCode()));
			}
			scopePks.add(scope.getPk());
			transactionCacheDAOService.emptyCache();
		}
		return scopePks;
	}

	public void fillDatabase(final ZonedDateTime origin, final Integer scale, final DatabaseActionContext context) {
		final var study = studyService.getStudy();

		// TODO build an actual dependency tree for scope models, the current way does not allow to have sibling scopes
		//retrieve all scope models that are not virtual and not root, sort them in order of depth
		final var scopeModels = study.getScopeModels().stream()
			.filter(s -> !s.isVirtual() && !s.isRoot())
			.sorted(Comparator.comparing(ScopeModel::getDepth))
			.toList();

		var parentPks = Collections.singletonList(scopeService.getRootScope().getPk());
		var counter = 0;
		for(final var scopeModel : scopeModels) {
			//the global scale parameter matches the number of leaf scopes
			//each parent model has 10 times less scopes than its children
			var subscale = (int) (scale / Math.pow(10, scopeModels.size() - 1 - counter));
			//there is always at least one scope model
			subscale = subscale == 0 ? 1 : subscale;

			//create scopes
			logger.info("Creating {} {}", subscale, scopeModel.getId());

			parentPks = generateScopes(context, origin, parentPks, scopeModel, subscale);
			counter++;
		}
	}

	@Transactional
	public void fillDatabase(final Integer scale) {
		final var origin = ZonedDateTime.now();
		final var context = auditActionService.createAuditActionAndGenerateContext(Actor.SYSTEM, "Add randomly generated data");
		fillDatabase(origin, scale, context);
	}
}
