package ch.rodano.configuration.builder;

import java.util.Arrays;
import java.util.Collections;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.event.EventModel;
import ch.rodano.configuration.model.feature.Feature;
import ch.rodano.configuration.model.field.FieldModel;
import ch.rodano.configuration.model.field.FieldModelType;
import ch.rodano.configuration.model.form.FormModel;
import ch.rodano.configuration.model.language.Language;
import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.model.menu.Menu;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.study.Study;

public class StudyBuilder {
	private final Study study;

	public static final String STUDY_ID = "TEST_STUDY";

	public static final String LANGUAGE_ID = LanguageStatic.en.name();
	public static final String LANGUAGE_NAME = "English";

	public static final String COUNTRY_ID = "FR";
	public static final String COUNTRY_NAME = "France";

	public static final String PROFILE_ID = "ADMIN";
	public static final String FEATURE_ID = "EXPORT";

	public static final String MENU_HOME_PAGE_ID = "HOME";
	public static final String MENU_SCOPE_ID = "SCOPES";

	public static final String SCOPE_MODEL_STUDY_ID = "STUDY";
	public static final String SCOPE_MODEL_COUNTRY_ID = "COUNTRY";
	public static final String SCOPE_MODEL_CENTER_ID = "CENTER";
	public static final String SCOPE_MODEL_PATIENT_ID = "PATIENT";

	public static final String EVENT_MODEL_ID = "TEST_EVENT_MODEL";
	public static final String DATASET_MODEL_ID = "TEST_DOCUMENT_MODEL";
	public static final String FIELD_MODEL_ID = "TEST_FIELD_MODEL";
	public static final String PAGE_ID = "TEST_PAGE";

	private StudyBuilder(final Study study) {
		this.study = study;
	}

	public Study getStudy() {
		return study;
	}

	public static StudyBuilder buildSimpleStudy() {
		//study
		final var study = new Study();
		study.getShortname().put(LANGUAGE_ID, STUDY_ID);
		study.setId(STUDY_ID);

		//language
		final var language = new Language();
		language.setId(LANGUAGE_ID);
		language.setName(LANGUAGE_NAME);

		language.setStudy(study);
		study.getLanguages().add(language);

		study.setLanguageIds(Collections.singletonList(LANGUAGE_ID));
		study.setDefaultLanguage(language);

		return new StudyBuilder(study);
	}

	public static StudyBuilder buildStudy() {
		//study
		final var builder = buildSimpleStudy();

		//feature and profile
		builder.addFeature(FEATURE_ID, FEATURE_ID);
		builder.addProfile(PROFILE_ID, PROFILE_ID);
		//assign feature to profile
		builder.getStudy().getProfile(PROFILE_ID).addAssignableToProfileItem(builder.getStudy().getFeature(FEATURE_ID));

		//scope models
		builder.addScopeModel(SCOPE_MODEL_STUDY_ID, SCOPE_MODEL_STUDY_ID, null);
		builder.addScopeModel(SCOPE_MODEL_COUNTRY_ID, SCOPE_MODEL_COUNTRY_ID, SCOPE_MODEL_STUDY_ID);
		builder.addScopeModel(SCOPE_MODEL_CENTER_ID, SCOPE_MODEL_CENTER_ID, SCOPE_MODEL_COUNTRY_ID);
		builder.addScopeModel(SCOPE_MODEL_PATIENT_ID, SCOPE_MODEL_PATIENT_ID, SCOPE_MODEL_CENTER_ID);

		//event model
		builder.addEventModel(SCOPE_MODEL_PATIENT_ID, EVENT_MODEL_ID, EVENT_MODEL_ID);

		//document model
		builder.addDatasetModel(DATASET_MODEL_ID, DATASET_MODEL_ID);

		//field model
		builder.addFieldModel(DATASET_MODEL_ID, FIELD_MODEL_ID, FIELD_MODEL_ID);

		//form model
		builder.addFormModel(PAGE_ID, PAGE_ID);

		//menus
		builder.addMenu(MENU_HOME_PAGE_ID, MENU_HOME_PAGE_ID);
		builder.addMenu(MENU_SCOPE_ID, MENU_SCOPE_ID);

		final var menu1 = builder.getStudy().getMenu(MENU_HOME_PAGE_ID);
		menu1.setOrderBy(1);
		menu1.setHomePage(true);

		final var menu2 = builder.getStudy().getMenu(MENU_SCOPE_ID);
		menu2.setOrderBy(2);

		return builder;
	}

	public StudyBuilder addLanguage(final String languageId, final String languageName) {
		final var language = new Language();
		language.setId(languageId);
		language.setName(languageName);

		language.setStudy(study);
		study.getLanguages().add(language);

		return this;
	}

	public StudyBuilder addFeature(final String featureId, final String featureName) {
		final var feature = new Feature();
		feature.setId(featureId);
		feature.getShortname().put(LANGUAGE_ID, featureName);
		feature.getLongname().put(LANGUAGE_ID, featureName);

		feature.setStudy(study);
		study.getFeatures().add(feature);

		return this;
	}

	public StudyBuilder addProfile(final String profileId, final String profileName) {
		final var profile = new Profile();
		profile.setId(profileId);
		profile.getShortname().put(LANGUAGE_ID, profileName);
		profile.getLongname().put(LANGUAGE_ID, profileName);

		profile.setStudy(study);
		study.getProfiles().add(profile);

		return this;
	}

	public StudyBuilder addScopeModel(final String scopeModelId, final String scopeModelName, final String parentScopeModelId) {
		final var scopeModel = new ScopeModel();
		scopeModel.setId(scopeModelId);
		scopeModel.getShortname().put(LANGUAGE_ID, scopeModelName);
		scopeModel.getLongname().put(LANGUAGE_ID, scopeModelName);

		if(parentScopeModelId != null) {
			scopeModel.setParentIds(Collections.singletonList(parentScopeModelId));
			scopeModel.setDefaultParentId(parentScopeModelId);
		}

		scopeModel.setStudy(study);
		study.getScopeModels().add(scopeModel);

		return this;
	}

	public StudyBuilder addEventModel(final String scopeModelId, final String eventModelId, final String eventModelName) {
		final var eventModel = new EventModel();
		eventModel.setId(eventModelId);
		eventModel.getShortname().put(LANGUAGE_ID, eventModelName);
		eventModel.getLongname().put(LANGUAGE_ID, eventModelName);

		final var scopeModel = study.getScopeModel(scopeModelId);
		eventModel.setScopeModel(scopeModel);
		scopeModel.getEventModels().add(eventModel);

		return this;
	}

	public StudyBuilder addDatasetModel(final String datasetModelId, final String datasetModelName) {
		final var datasetModel = new DatasetModel();
		datasetModel.setId(datasetModelId);
		datasetModel.getShortname().put(LANGUAGE_ID, datasetModelId);
		datasetModel.getLongname().put(LANGUAGE_ID, datasetModelName);

		datasetModel.setStudy(study);
		study.getDatasetModels().add(datasetModel);

		return this;
	}

	public StudyBuilder addFieldModel(final String datasetModelId, final String fieldModelId, final String fieldModelName) {
		final var fieldModel = new FieldModel();
		fieldModel.setId(fieldModelId);
		fieldModel.getShortname().put(LANGUAGE_ID, fieldModelName);
		fieldModel.getLongname().put(LANGUAGE_ID, fieldModelName);

		final var datasetModel = study.getDatasetModel(datasetModelId);
		fieldModel.setDatasetModel(datasetModel);
		datasetModel.getFieldModels().add(fieldModel);

		return this;
	}

	public StudyBuilder addDatasetModel(final DatasetModel datasetModel) {
		datasetModel.setStudy(study);
		study.getDatasetModels().add(datasetModel);

		return this;
	}

	public StudyBuilder addFormModel(final String formModelId, final String formModelName) {
		final var formModel = new FormModel();
		formModel.setId(formModelId);
		formModel.getShortname().put(LANGUAGE_ID, formModelName);
		formModel.getLongname().put(LANGUAGE_ID, formModelName);

		formModel.setStudy(study);
		study.getFormModels().add(formModel);

		return this;
	}

	public StudyBuilder addMenu(final String menuId, final String menuName) {
		final var menu = new Menu();
		menu.setId(menuId);
		menu.getShortname().put(LANGUAGE_ID, menuName);
		menu.getLongname().put(LANGUAGE_ID, menuName);
		menu.setPublic(false);

		menu.setStudy(study);
		study.getMenus().add(menu);

		return this;
	}

	public static DatasetModel buildSimpleDatasetModel(final String datasetModelId) {
		final var datasetModel = new DatasetModel();
		datasetModel.setId(datasetModelId);
		datasetModel.getShortname().put(LANGUAGE_ID, datasetModelId);
		datasetModel.getDescription().put(LANGUAGE_ID, datasetModelId);

		return datasetModel;
	}

	public static DatasetModel buildSimpleDatasetModel(final String datasetModelId, final FieldModel... fieldModels) {
		final var datasetModel = buildSimpleDatasetModel(datasetModelId);

		Arrays.stream(fieldModels).forEach(fieldModel -> {
			fieldModel.setDatasetModel(datasetModel);
			datasetModel.getFieldModels().add(fieldModel);
		});

		return datasetModel;
	}

	public static FieldModel buildSimpleFieldModel(final String fieldModelId, final FieldModelType type) {
		final var fieldModel = new FieldModel();
		fieldModel.setId(fieldModelId);
		fieldModel.setType(type);

		return fieldModel;
	}

	public static FieldModel buildSimpleFieldModel(final String fieldModelId, final FieldModelType type, final boolean exportable) {
		final var fieldModel = buildSimpleFieldModel(fieldModelId, type);
		fieldModel.setExportable(exportable);

		return fieldModel;
	}
}
