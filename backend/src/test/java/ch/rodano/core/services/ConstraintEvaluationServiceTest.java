package ch.rodano.core.services;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.rules.data.ConstraintEvaluationService;
import ch.rodano.core.model.rules.data.DataEvaluation;
import ch.rodano.core.model.rules.data.DataState;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
@Transactional
public class ConstraintEvaluationServiceTest extends DatabaseTest {

	@Autowired
	private ScopeDAOService scopeDAOService;

	@Autowired
	private DatasetService datasetService;

	@Autowired
	private FieldService fieldService;

	@Autowired
	private ConstraintEvaluationService constraintEvaluationService;

	@Test
	@DisplayName("Data state dependencies are assembled correctly")
	public void testDependencies() {
		final var study = studyService.getStudy();
		final var validator = study.getValidator("AFTER_FIRST_SYMPTOMS");
		final var patient = scopeDAOService.getScopeByCode("FR-01-03");

		// create a new dataset and get a precise field
		final var dmtDataset = datasetService.create(patient, study.getDatasetModel("DMT_GRID"), context, "Rule test");
		final var beginDateFieldModel = dmtDataset.getDatasetModel().getFieldModel("DMT_BEGIN_DATE");
		final var beginDateField = fieldService.get(dmtDataset, beginDateFieldModel);

		// create a data state
		final var dataState = new DataState(patient, Optional.empty(), dmtDataset, beginDateField);

		// evaluate it
		final var dataEvaluation = new DataEvaluation(dataState, validator.getConstraint());
		constraintEvaluationService.evaluate(dataEvaluation);

		assertTrue(dataEvaluation.isValid());

		// there are two dependencies for this validator, DATE_OF_FIRST_SYMPTOMS value and the value itself
		assertEquals(2, dataEvaluation.getDependencies().size());

		// consider only the appropriate value
		final var evaluable = dataEvaluation.getDependencies().stream().filter(d -> d.getId().equals("DATE_OF_FIRST_SYMPTOMS")).findAny();
		assertTrue(evaluable.isPresent());
		assertTrue(evaluable.get() instanceof Field);
	}
}
