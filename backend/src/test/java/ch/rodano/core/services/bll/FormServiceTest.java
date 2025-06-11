package ch.rodano.core.services.bll;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.form.FormService;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;
import ch.rodano.test.TestHelperService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
@Transactional
public class FormServiceTest extends DatabaseTest {

	@Autowired
	private FormService formService;

	@Autowired
	private TestHelperService testHelperService;

	private Scope center;

	@BeforeEach
	public void initTest() {
		center = testHelperService.createCenter(context);
	}

	@Test
	@DisplayName("Form creation works")
	public void testFormCreation() {
		final var patient = testHelperService.createPatient(center, context);

		final var childrenFormModel = studyService.getStudy().getFormModel("CHILDREN");
		final var chilrenForm = formService.create(patient, childrenFormModel, context, TEST_RATIONALE);

		final var childrenForms = formService.getAllIncludingRemoved(patient).stream()
			.filter(f -> f.getFormModelId().equals("CHILDREN"))
			.toList();
		assertEquals(1, childrenForms.size());
		assertEquals(childrenFormModel, chilrenForm.getFormModel());
		assertFalse(chilrenForm.getDeleted());
		assertEquals(patient.getPk(), chilrenForm.getScopeFk());
	}

	@Test
	@DisplayName("Form deletion works")
	public void testFormDeletion() {
		final var patient = testHelperService.createPatient(center, context);

		final var childrenFormModel = studyService.getStudy().getFormModel("CHILDREN");
		final var chilrenForm = formService.create(patient, childrenFormModel, context, TEST_RATIONALE);
		formService.delete(patient, Optional.empty(), chilrenForm, context, TEST_RATIONALE);

		final var childrenForms = formService.getAllIncludingRemoved(patient).stream()
			.filter(f -> f.getFormModelId().equals("CHILDREN"))
			.toList();
		assertEquals(1, childrenForms.size());
		assertTrue(chilrenForm.getDeleted());
	}
}
