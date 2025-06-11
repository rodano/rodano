package ch.rodano.configuration.model.study;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collections;
import java.util.TreeMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import ch.rodano.configuration.builder.StudyBuilder;
import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.feature.Feature;
import ch.rodano.configuration.model.language.Language;
import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.model.payment.PaymentDistribution;
import ch.rodano.configuration.model.payment.PaymentPlan;
import ch.rodano.configuration.model.payment.PaymentStep;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.validator.Validator;
import ch.rodano.configuration.utils.DisplayableUtils;

@Tag("configuration")
public class StudyTest {
	private static final String LANGUAGE = LanguageStatic.en.name();

	@Test
	@DisplayName("Check languages")
	public void checkLanguages() {
		//Create study
		final var study = StudyBuilder.buildStudy().getStudy();

		//existing language
		assertAll("Existing language",
			() -> assertEquals("en", study.getLanguage("en").getId()),
			() -> assertEquals(1, study.getLanguages().size()),

			() -> assertEquals("en", study.getDefaultLanguage().getId()),
			() -> assertEquals("en", study.getDefaultLanguageId()),
			() -> assertEquals(study.getLanguageStatic(LanguageStatic.en), study.getDefaultLanguage())
			);

		//new language
		final var language = new Language();
		language.setId("de");

		//add language in study
		language.setStudy(study);
		study.getLanguages().add(language);

		assertAll("Add language to study",
			() -> assertEquals(2, study.getChildrenWithEntity(Entity.LANGUAGE).size()),
			() -> assertEquals("", language.getLocalizedLongname(LanguageStatic.en.getId())),
			() -> assertEquals("", language.getLocalizedShortname(LanguageStatic.en.getId())),
			() -> assertEquals("", language.getLocalizedDescription(LanguageStatic.en.getId()))
			);

		language.setShortname(new TreeMap<>(Collections.singletonMap(LanguageStatic.en.getId(), "Germany")));

		assertAll("Set short name",
			() -> assertEquals("Germany", language.getLocalizedShortname(LanguageStatic.en.getId())),
			() -> assertEquals("", language.getLocalizedShortname(LanguageStatic.fr.getId())),
			() -> assertEquals("", language.getLocalizedLongname(LanguageStatic.en.getId())),
			() -> assertEquals("", language.getLocalizedDescription(LanguageStatic.en.getId()))
			);

		study.setDefaultLanguage(language);

		assertAll("Set default language",
			() -> assertEquals("Germany", language.getLocalizedShortname(LanguageStatic.en.getId())),
			() -> assertEquals("", language.getLocalizedShortname(LanguageStatic.fr.getId())),
			() -> assertEquals("", language.getLocalizedLongname(LanguageStatic.en.getId())),

			() -> assertEquals("Germany (default)", language.getLocalizedLabel(LanguageStatic.en.getId())),
			() -> assertEquals(" (défaut)", language.getLocalizedLabel(LanguageStatic.fr.getId()))
			);

		study.setDefaultLanguageId(StudyBuilder.LANGUAGE_ID);
	}

	@Test
	@DisplayName("Check features")
	public void checkFeatures() {
		//Create study
		final var study = StudyBuilder.buildStudy().getStudy();

		//existing feature
		assertEquals(1, study.getFeatures().size());

		//new feature
		final var feature = new Feature();
		feature.setId("LOGIN");

		feature.setStudy(study);
		study.getFeatures().add(feature);

		assertEquals(2, study.getFeatures().size());

		assertAll(
			() -> assertEquals("", study.getFeature("LOGIN").getLocalizedShortname(LANGUAGE)),
			() -> assertEquals("", study.getFeature("LOGIN").getLocalizedLongname(LANGUAGE)),
			() -> assertEquals("", study.getFeature("LOGIN").getLocalizedDescription(LANGUAGE))
			);

		feature.getShortname().put(LANGUAGE, "Login Shortname");
		feature.getLongname().put(LANGUAGE, "Login Longname");
		feature.getDescription().put(LANGUAGE, "Login Description");

		assertAll(
			() -> assertEquals("", DisplayableUtils.getLocalizedMap(null, LANGUAGE)),
			() -> assertEquals("Login Shortname", DisplayableUtils.getLocalizedMap(feature.getShortname(), LANGUAGE)),
			() -> assertEquals("", DisplayableUtils.getLocalizedMap(feature.getShortname(), LanguageStatic.fr.name()))
			);

		assertAll(
			() -> assertEquals("Login Shortname", study.getFeature("LOGIN").getLocalizedShortname(LANGUAGE)),
			() -> assertEquals("Login Longname", study.getFeature("LOGIN").getLocalizedLongname(LANGUAGE)),
			() -> assertEquals("Login Description", study.getFeature("LOGIN").getLocalizedDescription(LANGUAGE))
			);

		//other new feature
		final var feature2 = new Feature();
		feature2.setId("TEST_FEATURE");

		//add feature in study
		feature2.setStudy(study);
		study.getFeatures().add(feature2);

		assertAll("Add feature in study",
			() -> assertNotNull(study.getFeature("TEST_FEATURE")),
			() -> assertFalse(study.getFeaturesStatic().contains(feature2)),
			() -> assertTrue(study.getFeaturesDynamic().contains(feature2))
			);
	}

	@Test
	@DisplayName("Check profiles")
	public void checkProfiles() {
		//Create study
		final var study = StudyBuilder.buildStudy().getStudy();

		//existing profile
		assertEquals("ADMIN", study.getProfile("ADMIN").getId());

		//new profile
		final var profile = new Profile();
		profile.setId("TEST_PROFILE");

		profile.setStudy(study);
		study.getProfiles().add(profile);

		assertEquals(2, study.getProfiles().size());

		//Create a feature
		final var feature = new Feature();
		feature.setId("TEST_FEATURE");

		//add feature in study
		feature.setStudy(study);
		study.getFeatures().add(feature);

		//assign feature to profile
		profile.addAssignableToProfileItem(feature);

		assertEquals(1, study.getProfile("TEST_PROFILE").getFeatures().size());
		assertEquals(feature, study.getProfile("TEST_PROFILE").getFeatures().get(0));
	}

	@Test
	@DisplayName("Check scope models")
	public void checkScopeModels() {
		//Create study
		final var study = StudyBuilder.buildStudy().getStudy();

		//existing scope models
		assertAll("Existing scope models",
			() -> assertEquals(4, study.getScopeModels().size()),
			() -> assertEquals(StudyBuilder.SCOPE_MODEL_STUDY_ID, study.getRootScopeModel().getId()),
			() -> assertEquals(StudyBuilder.SCOPE_MODEL_PATIENT_ID, study.getLeafScopeModel().getId())
			);

		final var studyModel = study.getScopeModel("STUDY");
		final var country = study.getScopeModel("COUNTRY");
		final var center = study.getScopeModel("CENTER");
		final var patient = study.getScopeModel("PATIENT");

		assertEquals(center, patient.getDefaultParent());
		assertEquals(country, center.getDefaultParent());

		assertThrows(Exception.class, studyModel::getDefaultParent, "We should not be able to retrieve default parent for study");

		assertAll("Default ancestors",
			() -> assertEquals(3, patient.getDefaultAncestors().size()),
			() -> assertEquals(2, center.getDefaultAncestors().size()),
			() -> assertTrue(center.getDefaultAncestors().contains(country)),
			() -> assertTrue(center.getDefaultAncestors().contains(studyModel)),
			() -> assertFalse(center.getDefaultAncestors().contains(patient)),
			() -> assertTrue(studyModel.getDefaultAncestors().isEmpty())
			);

		assertAll("Ancestors",
			() -> assertEquals(0, studyModel.getScopeModelAncestors().size()),
			() -> assertEquals(3, patient.getScopeModelAncestors().size()),
			() -> assertEquals(1, patient.getScopeModelParents().size()),
			() -> assertEquals(center, patient.getScopeModelParents().get(0))
			);

		assertAll("Children",
			() -> assertEquals(1, studyModel.getChildrenScopeModel().size()),
			() -> assertEquals(3, studyModel.getDescendantsScopeModel().size()),
			() -> assertEquals(0, patient.getChildrenScopeModel().size())
			);

		assertAll("Is ... of",
			() -> assertTrue(patient.isDescendantOf(studyModel)),
			() -> assertTrue(studyModel.isAncestorOf(patient)),
			() -> assertTrue(center.isParentOf(patient))
			);

		assertEquals(1, studyModel.getChildrenWithEntity(Entity.SCOPE_MODEL).size());
	}

	@Test
	@DisplayName("Check menus")
	public void checkMenus() {
		//Create study
		final var study = StudyBuilder.buildStudy().getStudy();

		assertEquals(StudyBuilder.MENU_HOME_PAGE_ID, study.getPrivateHomePage().getId());
		assertEquals(StudyBuilder.MENU_HOME_PAGE_ID, study.getMenus().first().getId());
		assertFalse(study.hasPublicPage());

		try {
			study.getPublicHomePage();
			fail("Study has a public home page");
		}
		catch(final Exception e) {
			//success
		}
	}

	@Test
	@DisplayName("Check validators")
	public void checkValidators() {
		//Create study
		final var study = StudyBuilder.buildStudy().getStudy();

		//create validator
		final var validator = new Validator();
		validator.setId("TEST_VALIDATOR");

		//add validator in study
		validator.setStudy(study);
		study.getValidators().add(validator);

		validator.getShortname().put(study.getDefaultLanguage().getId(), "Test Validator");
		validator.getLongname().put(study.getDefaultLanguage().getId(), "Test Validator");
		validator.getDescription().put(study.getDefaultLanguage().getId(), "Test Validator");

		assertEquals("Test Validator", validator.getLocalizedShortname(study.getDefaultLanguage().getId()));
		assertEquals("", validator.getLocalizedShortname("TOTO"));
	}

	@Test
	@DisplayName("Check payment plan")
	public void checkPaymentPlan() {
		//Create study
		final var study = StudyBuilder.buildStudy().getStudy();

		//create plan
		final var plan = new PaymentPlan();
		plan.setId("PLAN1");
		assertEquals(0, plan.getSteps().size());

		//add plan in study
		plan.setStudy(study);
		study.getPaymentPlans().add(plan);
		assertEquals(1, study.getPaymentPlans().size());

		assertNotNull(study.getPaymentPlan("PLAN1"));
		try {
			study.getPaymentPlan("DUMMY_PLAN");
			fail("Found non existent plan DUMMY_PLAN");
		}
		catch(final Exception e) {
			//success
		}

		final var step1 = new PaymentStep();
		step1.setId("STEP1");
		step1.setPaymentPlan(plan);

		plan.getSteps().add(step1);

		assertEquals("STEP1", plan.getStepFromId("STEP1").getId());
		try {
			assertEquals("STEP2", plan.getStepFromId("STEP2").getId());
			fail("Step does not exist");
		}
		catch(final Exception e) {
			//success
		}

		plan.setWorkflow("DE_VISIT");
		plan.getStepFromId("STEP1").setWorkflowable("BASELINE");
		plan.setCurrency("EUR");

		final var distribution = new PaymentDistribution();
		distribution.setScopeModelId("Center");
		distribution.setStep(step1);

		plan.getStepFromId("STEP1").getDistributions().add(distribution);
	}
}

