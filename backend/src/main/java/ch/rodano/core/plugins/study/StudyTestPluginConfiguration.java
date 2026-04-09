package ch.rodano.core.plugins.study;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import ch.rodano.configuration.model.field.PartialDate;
import ch.rodano.configuration.model.field.PossibleValue;
import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.plugins.export.ExportPlugin;
import ch.rodano.core.plugins.pv.PossibleValuesPlugin;
import ch.rodano.core.plugins.validators.ValidatorPlugin;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.file.FileService;
import ch.rodano.core.services.bll.study.StudyService;

@Configuration
public class StudyTestPluginConfiguration {
	private static final Logger LOGGER = LoggerFactory.getLogger(StudyTestExportPlugin.class);

	/**
	 * This is the study-test validator
	 * It is part of rodano-backend because we can then start the test study without having to checkout study-test
	 * <p>
	 * This bean is initialized when rodano-backend alone (without a study) via the @Component annotation
	 * <p>
	 * When a study is started (except for the test study), this bean is not initialized thanks to @ConditionalOnMissingBean which avoid starting
	 * this bean when another ValidatorPlugin exists (in the study)
	 * <p>
	 * Therefore when creating a study, it is mandatory to create a study-specific ValidatorPlugin (even an empty one)
	 */
	@Bean
	@ConditionalOnMissingBean(ValidatorPlugin.class)
	public ValidatorPlugin validatorPlugin(@Lazy final DatasetService datasetService, @Lazy final FieldService fieldService, @Lazy final FileService fileService) {
		return new StudyTestValidatorPlugin(datasetService, fieldService, fileService);
	}

	static String resolveFieldValue(final Field f) {
		final String value = f.getValue();
		if(StringUtils.isBlank(value)) {
			return value;
		}
		try {
			UUID.fromString(value);
			return f.getFieldModel().getPossibleValues().stream()
				.filter(pv -> value.equals(pv.getPossibleValueId() != null ? pv.getPossibleValueId().toString() : null))
				.map(PossibleValue::getId)
				.findFirst()
				.orElse(value);
		}
		catch(IllegalArgumentException e) {
			return value;
		}
	}

	public static class StudyTestValidatorPlugin implements ValidatorPlugin {
		final DatasetService datasetService;
		final FieldService fieldService;
		final FileService fileService;

		public StudyTestValidatorPlugin(final DatasetService datasetService, final FieldService fieldService, final FileService fileService) {
			this.datasetService = datasetService;
			this.fieldService = fieldService;
			this.fileService = fileService;
		}

		@SuppressWarnings("unused")
		public boolean FEMALE_EMPLOYED(final Scope scope, final Optional<Event> event, final Dataset dataset, final Field field) {
			final var genderFieldModel = dataset.getDatasetModel().getFieldModel("GENDER");
			final var gender = fieldService.get(dataset, genderFieldModel);

			final var femalePossibleValue = genderFieldModel.getPossibleValues().stream()
				.filter(pv -> "FEMALE".equals(pv.getId()))
				.findFirst()
				.orElse(null);

			if(femalePossibleValue == null) {
				return true;
			}

			final String femaleUuid = femalePossibleValue.getPossibleValueId().toString();

			final var employedPossibleValue = field.getFieldModel().getPossibleValues().stream()
				.filter(pv -> "EMPLOYED".equals(pv.getId()))
				.findFirst()
				.orElse(null);

			if(employedPossibleValue == null) {
				return true;
			}

			final String employedUuid = employedPossibleValue.getPossibleValueId().toString();

			return !femaleUuid.equals(gender.getValue()) || femaleUuid.equals(gender.getValue()) && employedUuid.equals(field.getValue());
		}

		@SuppressWarnings("unused")
		public boolean FILE_IS_PDF(final Scope scope, final Optional<Event> event, final Dataset dataset, final Field field) {
			final var file = fileService.getFile(field);
			//no check to do if there is no file
			if(file == null) {
				return true;
			}
			try(final var is = new FileInputStream(fileService.getStoredFile(file))) {
				final byte[] magicBytes = is.readNBytes(4);
				final var header = new String(magicBytes);
				return "%PDF".equals(header);
			}
			catch(final IOException e) {
				return true;
			}
		}
	}

	/**
	 * This is the study-test validator
	 * It is part of rodano-backend because we can then start the test study without having to checkout study-test
	 * <p>
	 * This bean is initialized when rodano-backend alone (without a study) via the @Component annotation
	 * <p>
	 * When a study is started (except for the test study), this bean is not initialized thanks to @ConditionalOnMissingBean which avoid starting
	 * this bean when another PossibleValuesPlugin exists (in the study)
	 * <p>
	 * Therefore when creating a study, it is mandatory to create a study-specific PossibleValuesPlugin (even an empty one)
	 */
	@Bean
	@ConditionalOnMissingBean(ExportPlugin.class)
	public ExportPlugin exportPlugin(@Lazy final DatasetService datasetService, @Lazy final FieldService fieldService) {
		return new StudyTestExportPlugin(datasetService, fieldService);
	}

	public static class StudyTestExportPlugin implements ExportPlugin {
		final DatasetService datasetService;
		final FieldService fieldService;

		public StudyTestExportPlugin(final DatasetService datasetService, final FieldService fieldService) {
			this.datasetService = datasetService;
			this.fieldService = fieldService;
		}

		@SuppressWarnings("unused")
		public String EDSS_CALC(final Scope scope, final Optional<Event> event, final Dataset dataset, final Field field) {
			final var datasetModel = dataset.getDatasetModel();

			final var kfs1Raw = fieldService.get(dataset, datasetModel.getFieldModel("KFS1"));
			final var kfs2Raw = fieldService.get(dataset, datasetModel.getFieldModel("KFS2"));
			final var kfs3Raw = fieldService.get(dataset, datasetModel.getFieldModel("KFS3"));
			final var kfs4Raw = fieldService.get(dataset, datasetModel.getFieldModel("KFS4"));
			final var kfs5Raw = fieldService.get(dataset, datasetModel.getFieldModel("KFS5"));
			final var kfs6Raw = fieldService.get(dataset, datasetModel.getFieldModel("KFS6"));
			final var kfs7Raw = fieldService.get(dataset, datasetModel.getFieldModel("KFS7"));
			final var ambulationRaw = fieldService.get(dataset, datasetModel.getFieldModel("AMBULATION"));

			final var kfs1 = resolveFieldValue(kfs1Raw);
			final var kfs2 = resolveFieldValue(kfs2Raw);
			final var kfs3 = resolveFieldValue(kfs3Raw);
			final var kfs4 = resolveFieldValue(kfs4Raw);
			final var kfs5 = resolveFieldValue(kfs5Raw);
			final var kfs6 = resolveFieldValue(kfs6Raw);
			final var kfs7 = resolveFieldValue(kfs7Raw);
			final var ambulation = resolveFieldValue(ambulationRaw);

			if(StringUtils.isAnyBlank(kfs1, kfs2, kfs3, kfs4, kfs5, kfs6, kfs7, ambulation)) {
				LOGGER.info("Unable to calculate plugin [EDSS_CALC] because of missing values");
				return "";
			}

			final var score = Stream.of(kfs2, kfs2, kfs3, kfs4, kfs5, kfs6, kfs7, ambulation)
				.mapToDouble(Double::valueOf).average().orElseThrow();
			return field.getFieldModel().objectToString(score);
		}

		@SuppressWarnings("unused")
		public String AGE_AT_DIAGNOSIS(final Scope scope, final Optional<Event> event, final Dataset dataset, final Field field) {
			final var datasetModel = dataset.getDatasetModel();
			final var birthDateField = fieldService.get(dataset, datasetModel.getFieldModel("BIRTH_DATE"));
			final var dateOfFirstSymptomsField = fieldService.get(dataset, datasetModel.getFieldModel("DATE_OF_DIAGNOSIS"));
			if(birthDateField.isBlank() || dateOfFirstSymptomsField.isBlank()) {
				LOGGER.info("Unable to calculate plugin [AGE_AT_DIAGNOSIS] because BIRTH_DATE and/or DATE_OF_DIAGNOSIS is blank");
				return "";
			}
			//retrieve date values
			final var birthDate = (PartialDate) birthDateField.getObjectValue();
			final var dateOfDiagnosis = (PartialDate) dateOfFirstSymptomsField.getObjectValue();
			if(!birthDate.isAnchoredInTime() || !dateOfDiagnosis.isAnchoredInTime()) {
				LOGGER.info("Unable to calculate plugin [AGE_AT_DIAGNOSIS] because BIRTH_DATE and/or DATE_OF_DIAGNOSIS is not a valid date");
				return "";
			}

			return String.valueOf(ChronoUnit.YEARS.between(birthDate.toZonedDateTime().get(), dateOfDiagnosis.toZonedDateTime().get()));
		}
	}

	/**
	 * This is the study-test possible values provider
	 * It is part of rodano-backend because we can then start the test study without having to checkout study-test
	 * <p>
	 * This bean is initialized when rodano-backend alone (without a study) via the @Component annotation
	 * <p>
	 * When a study is started (except for the test study), this bean is not initialized thanks to @ConditionalOnMissingBean which avoid starting
	 * this bean when another PossibleValuesPlugin exists (in the study)
	 * <p>
	 * Therefore when creating a study, it is mandatory to create a study-specific PossibleValuesPlugin (even an empty one)
	 */
	@Bean
	@ConditionalOnMissingBean(PossibleValuesPlugin.class)
	public PossibleValuesPlugin possibleValuesPlugin(
		@Lazy final StudyService studyService,
		@Lazy final EventService eventService,
		@Lazy final DatasetService datasetService,
		@Lazy final FieldService fieldService
	) {
		return new StudyTestPossibleValuesPlugin(studyService, eventService, datasetService, fieldService);
	}

	public static class StudyTestPossibleValuesPlugin implements PossibleValuesPlugin {
		final StudyService studyService;
		final EventService eventService;
		final DatasetService datasetService;
		final FieldService fieldService;

		public StudyTestPossibleValuesPlugin(final StudyService studyService, final EventService eventService, final DatasetService datasetService, final FieldService fieldService) {
			this.studyService = studyService;
			this.eventService = eventService;
			this.datasetService = datasetService;
			this.fieldService = fieldService;
		}

		@SuppressWarnings("unused")
		public List<PossibleValue> STEPS_DEPENDING_ON_AMBULATION(final Scope scope, final Optional<Event> event, final Dataset dataset, final Field field) {
			//retrieve previous EDSS value
			Optional<Event> previousEvent = Optional.empty();
			final var events = new TreeSet<>(eventService.getAll(scope));
			for(final var e : events) {
				if("MEDICAL_VISITS".equals(e.getEventModel().getEventGroupId())) {
					if(!e.isExpected() && e.getDate().isBefore(event.get().getDateOrExpectedDate())) {
						previousEvent = Optional.of(e);
					}
				}
			}

			int ambulationValue = 0;
			if(previousEvent.isPresent()) {
				final var study = studyService.getStudy();
				final var datasetModel = study.getDatasetModel("VISIT_DOCUMENTATION");

				final var VISIT_DOCUMENTATION = datasetService.get(previousEvent.get(), datasetModel);
				final var ambulationFieldModel = VISIT_DOCUMENTATION.getDatasetModel().getFieldModel("AMBULATION");
				final var AMBULATION = fieldService.get(VISIT_DOCUMENTATION, ambulationFieldModel);
				final var ambulationValueStr = resolveFieldValue(AMBULATION);
				ambulationValue = StringUtils.isBlank(ambulationValueStr) ? 0 : Integer.parseInt(ambulationValueStr);
			}

			final List<PossibleValue> possibleValues = new ArrayList<>();

			if(ambulationValue < 7) {
				possibleValues.add(
					new PossibleValue(
						"UNKNOWN",
						Map.of(LanguageStatic.en.name(), "Unknown")
					)
				);

				possibleValues.add(
					new PossibleValue(
						"LT500S",
						Map.of(LanguageStatic.en.name(), "Less than 500 steps")
					)
				);
				possibleValues.add(
					new PossibleValue(
						"LT1000S",
						Map.of(LanguageStatic.en.name(), "Less than 1000 steps")
					)
				);
				if(ambulationValue < 5) {
					possibleValues.add(
						new PossibleValue(
							"LT5000S",
							Map.of(LanguageStatic.en.name(), "Less than 5000 steps")
						)
					);
					possibleValues.add(
						new PossibleValue(
							"LT51000S",
							Map.of(LanguageStatic.en.name(), "Less than 10000 steps")
						)
					);
				}
			}
			else {
				possibleValues.add(
					new PossibleValue(
						"NA",
						Map.of(LanguageStatic.en.name(), "NA")
					)
				);
			}
			return possibleValues;
		}
	}

}
