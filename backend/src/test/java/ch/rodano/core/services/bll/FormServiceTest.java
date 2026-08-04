package ch.rodano.core.services.bll;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.configuration.model.form.FormModel;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.exception.MissingDataException;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.form.FormService;
import ch.rodano.core.services.dao.form.FormDAOService;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;
import ch.rodano.test.TestHelperService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
@Transactional
public class FormServiceTest extends DatabaseTest {

	@Autowired
	private FormService formService;

	@Autowired
	private FormDAOService formDAOService;

	@Autowired
	private TestHelperService testHelperService;

	private Scope center;

	private FormModel demographicsModel;

	private FormModel childrenFormModel;

	@BeforeEach
	public void initTest() {
		center = testHelperService.createCenter(context);
		demographicsModel = studyService.getStudy().getFormModel("DEMOGRAPHICS");
		childrenFormModel = studyService.getStudy().getFormModel("CHILDREN");
	}

	@Test
	@DisplayName("Form creation works")
	public void testFormCreation() {
		final var patient = testHelperService.createPatient(center, context);
		final var childrenForm = formService.create(patient, childrenFormModel, context, TEST_RATIONALE);
		assertEquals(childrenFormModel.getId(), childrenForm.getFormModelId());

		final var retrievedForm = formService.get(patient, childrenFormModel);
		assertNotNull(retrievedForm);
		assertFalse(retrievedForm.isRemoved());
		assertEquals(patient.getPk(), retrievedForm.getScopeFk());

		assertEquals(1, formDAOService.getAuditTrails(retrievedForm, Optional.empty(), Optional.empty()).size());
	}

	@Test
	@DisplayName("Form deletion works")
	public void testFormDeletion() {
		final var patient = testHelperService.createPatient(center, context);
		final var childrenForm = formService.create(patient, childrenFormModel, context, TEST_RATIONALE);

		//delete form
		final var deletionRationale = "Deletion";
		formService.delete(patient, Optional.empty(), childrenForm, context, deletionRationale);
		assertTrue(childrenForm.isRemoved());

		assertThrows(MissingDataException.class, () -> formService.get(patient, childrenFormModel));

		//check the deleted flag and audit trail
		final var trails = formDAOService.getAuditTrails(childrenForm, Optional.empty(), Optional.empty());
		assertEquals(2, trails.size());
		final var trail = trails.last();
		assertEquals(Actor.SYSTEM_USERNAME, trail.getAuditActor());
		assertEquals(String.format("Form removed: %s", deletionRationale), trail.getAuditContext());
		assertTrue(trail.isRemoved());
	}

	@Test
	@DisplayName("Form restoration works")
	public void testFormRestoration() {
		final var patient = testHelperService.createPatient(center, context);
		final var childrenForm = formService.create(patient, childrenFormModel, context, TEST_RATIONALE);

		//delete form
		formService.delete(patient, Optional.empty(), childrenForm, context, TEST_RATIONALE);

		//restore form
		final var restorationRationale = "Restoration";
		formService.restore(patient, Optional.empty(), childrenForm, context, restorationRationale);
		assertFalse(childrenForm.isRemoved());

		final var retrievedForm = formService.get(patient, childrenFormModel);
		assertNotNull(retrievedForm);
		assertFalse(retrievedForm.isRemoved());

		//check the deleted flag and audit trail
		final var trails = formDAOService.getAuditTrails(childrenForm, Optional.empty(), Optional.empty());
		assertEquals(3, trails.size());
		final var trail = trails.last();
		assertEquals(Actor.SYSTEM_USERNAME, trail.getAuditActor());
		assertEquals(String.format("Form restored: %s", restorationRationale), trail.getAuditContext());
		assertFalse(trail.isRemoved());
	}

	@Test
	@DisplayName("Scope form retrieval works")
	public void testRetrieveForms() {
		final var patient = testHelperService.createPatient(center, context);
		var forms = formService.getAll(patient);

		assertFalse(forms.isEmpty());
		final var formNumber = forms.size();

		var form = formService.get(patient, demographicsModel);
		assertNotNull(form);
		assertEquals(demographicsModel.getId(), form.getFormModelId());

		final var childrenForm = formService.create(patient, childrenFormModel, context, TEST_RATIONALE);

		forms = formService.getAll(patient);
		assertEquals(formNumber + 1, forms.size());

		form = formService.get(patient, childrenFormModel);
		assertNotNull(form);
		assertEquals(childrenFormModel.getId(), form.getFormModelId());

		//delete form
		formService.delete(patient, Optional.empty(), childrenForm, context, TEST_RATIONALE);

		forms = formService.getAll(patient);
		assertEquals(formNumber, forms.size());

		forms = formService.getAllIncludingRemoved(patient);
		assertEquals(formNumber + 1, forms.size());

		assertThrows(MissingDataException.class, () -> formService.get(patient, childrenFormModel));
	}
}
