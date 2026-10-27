package ch.rodano.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.epro.EPROInvitationDTO;
import ch.rodano.api.epro.EproScopeDTO;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.test.ControllerTest;
import ch.rodano.test.SpringTestConfiguration;

@SpringTestConfiguration
public class EproControllerTest extends ControllerTest {

	private static final String CENTER_INVESTIGATOR_EMAIL = "test+iinves@rodano.ch";

	private final ParameterizedTypeReference<PagedResult<EproScopeDTO>> eproScopesType = new ParameterizedTypeReference<>() {
		//don't care
	};

	@Autowired
	private ScopeDAOService scopeDAOService;

	private PagedResult<EproScopeDTO> searchScopes() {
		return get("/epro/scopes?pageSize=" + MAX_PAGE_SIZE + "&pageIndex=0", eproScopesType);
	}

	@Test
	@DisplayName("Only the leaf scopes under the roles of the user are listed")
	public void scopesAreRestrictedToTheRolesOfTheUser() {
		authenticate(adminOnStudyEmail);
		final var allLeafScopes = scopeDAOService.getAllScopesByScopeModelId(studyService.getStudy().getLeafScopeModel().getId());
		assertEquals(allLeafScopes.size(), searchScopes().getObjects().size());

		authenticate(CENTER_INVESTIGATOR_EMAIL);
		final var scopes = searchScopes().getObjects();
		assertFalse(scopes.isEmpty());
		assertTrue(scopes.size() < allLeafScopes.size());
		assertTrue(scopes.stream().allMatch(s -> s.scope().getCode().startsWith("FR-01-")));
	}

	@Test
	@DisplayName("Listing the scopes never exposes the robot keys")
	public void keysAreNotExposed() {
		authenticate(CENTER_INVESTIGATOR_EMAIL);
		final var scope = scopeDAOService.getScopeByCode("FR-01-02");
		final var invitation = invite(scope.getPk());
		assertNotNull(invitation.key());

		final var body = client.get().uri("/epro/scopes").exchange().expectStatus().isOk().expectBody(String.class).returnResult().getResponseBody();
		assertFalse(body.contains(invitation.key()));
		assertFalse(body.toLowerCase().contains("\"key\""));
	}

	@Test
	@DisplayName("A scope is flagged as invited until its access is revoked")
	public void inviteAndRevoke() {
		authenticate(CENTER_INVESTIGATOR_EMAIL);
		final var scope = scopeDAOService.getScopeByCode("FR-01-01");

		invite(scope.getPk());
		assertTrue(isInvited(scope.getCode()));

		client.put().uri("/epro/{scopePk}/revoke", scope.getPk()).exchange().expectStatus().isNoContent();
		assertFalse(isInvited(scope.getCode()));

		//there is nothing to revoke anymore
		client.put().uri("/epro/{scopePk}/revoke", scope.getPk()).exchange().expectStatus().isNotFound();
	}

	@Test
	@DisplayName("Access can not be revoked outside of the scopes of the user")
	public void revokeIsRestrictedToTheRolesOfTheUser() {
		authenticate(adminOnStudyEmail);
		final var foreignScope = scopeDAOService.getScopeByCode("AT-01-01");
		invite(foreignScope.getPk());

		authenticate(CENTER_INVESTIGATOR_EMAIL);
		client.put().uri("/epro/{scopePk}/revoke", foreignScope.getPk()).exchange().expectStatus().is4xxClientError();

		authenticate(adminOnStudyEmail);
		assertTrue(isInvited(foreignScope.getCode()));
	}

	private EPROInvitationDTO invite(final Long scopePk) {
		return client.put().uri("/epro/{scopePk}/invite", scopePk).exchange().expectStatus().isOk().expectBody(EPROInvitationDTO.class).returnResult().getResponseBody();
	}

	private boolean isInvited(final String scopeCode) {
		return searchScopes().getObjects().stream()
			.filter(s -> s.scope().getCode().equals(scopeCode))
			.findFirst()
			.orElseThrow()
			.invited();
	}
}
