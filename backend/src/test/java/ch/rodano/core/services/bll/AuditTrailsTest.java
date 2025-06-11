package ch.rodano.core.services.bll;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
@Transactional
public class AuditTrailsTest extends DatabaseTest {
	@Autowired
	private ScopeService scopeService;

	@Autowired
	private ScopeDAOService scopeDAOService;

	@Test
	@DisplayName("Audit trails are recorded")
	public void auditTrailsWork() {
		final var newScope = createScope();

		final var scopeVersions = scopeDAOService.getAuditTrails(newScope, Optional.empty(), Optional.empty());
		assertEquals(1, scopeVersions.size());
	}

	@Test
	@DisplayName("Audit trails depict history correctly")
	public void auditTrailsDepictHistory() {
		final var newScope = createScope();

		//update ID
		final var secondId = "secondId";
		newScope.setId(secondId);

		final var secondContextRationale = "Update ID";
		final var secondContext = createDatabaseActionContext(secondContextRationale);
		scopeService.save(newScope, secondContext, secondContextRationale);

		//update ID again
		final var thirdId = "thirdId";
		newScope.setId(thirdId);

		final var thirdContextRationale = "Update ID again";
		final var thirdContext = createDatabaseActionContext(thirdContextRationale);
		scopeService.save(newScope, thirdContext, thirdContextRationale);

		final var scopeVersions = scopeDAOService.getAuditTrails(newScope, Optional.empty(), Optional.empty());
		assertEquals(3, scopeVersions.size());

		final var firstAuditTrail = scopeVersions.pollFirst();
		final var secondAuditTrail = scopeVersions.pollFirst();
		final var thirdAuditTrail = scopeVersions.pollFirst();

		assertAll(
			"Verify that the values of ID correspond to changes",
			() -> assertEquals(secondId, secondAuditTrail.getId()),
			() -> assertEquals(thirdId, thirdAuditTrail.getId())
		);

		assertAll(
			"Check that the audit date order is correct",
			() -> assertTrue(firstAuditTrail.getAuditDatetime().isBefore(secondAuditTrail.getAuditDatetime())),
			() -> assertTrue(secondAuditTrail.getAuditDatetime().isBefore(thirdAuditTrail.getAuditDatetime()))
		);

		assertAll(
			"Verify that the contexts are written correctly",
			() -> assertEquals(secondContextRationale, secondAuditTrail.getAuditContext()),
			() -> assertEquals(thirdContextRationale, thirdAuditTrail.getAuditContext())
		);
	}

	private Scope createScope() {
		final var centerScopeModel = studyService.getStudy().getScopeModel("CENTER");
		final var centerParent = scopeService.getAll(centerScopeModel).stream().findFirst().get();
		final var patientScopeModel = studyService.getStudy().getScopeModel("PATIENT");
		final var scopeCandidate = scopeService.createCandidate(patientScopeModel, ZonedDateTime.now(), centerParent);

		return scopeService.createFromCandidate(scopeCandidate, patientScopeModel, centerParent, context, "Create new scope");
	}
}
