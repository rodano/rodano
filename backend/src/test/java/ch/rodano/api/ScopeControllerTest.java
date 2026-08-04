package ch.rodano.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.scope.ScopeDTO;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.test.ControllerTest;
import ch.rodano.test.SpringTestConfiguration;

@SpringTestConfiguration
public class ScopeControllerTest extends ControllerTest {

	private final ParameterizedTypeReference<PagedResult<ScopeDTO>> paginatedScopesType = new ParameterizedTypeReference<>() {
		//don't care
	};

	@Autowired
	private ScopeService scopeService;

	@Autowired
	private ScopeDAOService scopeDAOService;

	private Scope france;
	private Scope frenchPatient1;

	// TODO replace this initialisation by proper scope creation
	@BeforeEach
	public void setup() {
		france = scopeDAOService.getScopeByCode("FR");
		frenchPatient1 = scopeDAOService.getScopeByCode("FR-01-01");
	}

	@Test
	@DisplayName("Test the search")
	public void scopesAreSearchable() {
		// login as an admin
		authenticate(adminOnStudyEmail);

		// look for all scopes
		var scopes = get("/scopes", PagedResult.class);

		final var allScopes = scopeDAOService.getAllScopes();
		assertEquals(allScopes.size(), scopes.getObjects().size());
	}

	@Test
	@Disabled
	@DisplayName("User who does not have the MANAGE_REMOVED_DATA feature can not see scopes that have a removed ancestor")
	public void canNotGetScopesWithRemovedAncestor() {
		// login as an investigator (who does not have the MANAGE_REMOVED_DATA feature
		authenticate(investigatorOnStudyEmail);

		// verify that all the patients are there first
		final var foundScopes = searchScopes(frenchPatient1.getScopeModelId(), france.getPk());

		// get all the scopes that belong to the specified ancestor
		final var allPatientsInCentre = scopeService.getAll(Collections.singletonList(frenchPatient1.getScopeModel()), Collections.singletonList(france));

		assertEquals(allPatientsInCentre.size(), foundScopes.getObjects().size());

		// delete the patient centre
		scopeService.delete(france, context, "Testing");

		// get all the scopes again. This time the result should be empty.
		final var foundScopesWithoutDeleted = searchScopes(frenchPatient1.getScopeModelId(), france.getPk());

		assertEquals(0, foundScopesWithoutDeleted.getObjects().size());
	}

	private PagedResult<ScopeDTO> searchScopes(
		final String scopeModelId,
		final Long ancestorPk
	) {
		return client
			.get()
			.uri("/scopes?scopeModelId={scopeModelId}&ancestorPks={ancestorPks}",
				Map.of(
					"scopeModelId", scopeModelId,
					"ancestorPks", new int[] { Math.toIntExact(ancestorPk) }
				))
			.exchange()
			.expectStatus().isOk()
			.expectBody(paginatedScopesType)
			.returnResult()
			.getResponseBody();
	}
}
