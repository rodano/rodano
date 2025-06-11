package ch.rodano.api;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.test.ControllerTest;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringTestConfiguration
public class ScopeControllerTest extends ControllerTest {

	@Autowired
	private ScopeService scopeService;

	@Autowired
	private ScopeDAOService scopeDAOService;

	private Scope france;
	private Scope frenchPatient1;

	// TODO replace this initialisation by proper scope creation as soon as KV-1456 is done
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
		final var response = restTemplate.exchange(
			"/scopes",
			HttpMethod.GET,
			null,
			PagedResult.class,
			Collections.emptyMap()
		);

		final var allScopes = scopeDAOService.getAllScopes();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(allScopes.size(), response.getBody().getObjects().size());
	}

	@Test
	// TODO re-enable this as soon KV-1456 is done
	@Disabled
	@DisplayName("User who does not have the MANAGE_DELETED_DATA feature can not see scopes that have a deleted ancestor")
	public void canNotGetScopesWithDeletedAncestor() {
		// login as an investigator (who does not have the MANAGE_DELETED_DATA feature
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

	private PagedResult searchScopes(
		final String scopeModelId,
		final Long ancestorPk
	) {
		final var response = restTemplate.exchange(
			"/scopes?scopeModelId={scopeModelId}&ancestorPks={ancestorPks}",
			HttpMethod.GET,
			null,
			PagedResult.class,
			Map.of(
				"scopeModelId", scopeModelId,
				"ancestorPks", new int[] { Math.toIntExact(ancestorPk) }
			)
		);

		return response.getBody();
	}
}
