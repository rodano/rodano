package ch.rodano.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;

import ch.rodano.api.configuration.handler.ErrorDetails;
import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.resource.ResourceDTO;
import ch.rodano.api.resource.ResourceSubmissionDTO;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.test.ControllerTest;
import ch.rodano.test.SpringTestConfiguration;

@SpringTestConfiguration
class ResourceControllerTest extends ControllerTest {

	@Autowired
	private ScopeDAOService scopeDAOService;

	private final ParameterizedTypeReference<PagedResult<ResourceDTO>> paginatedResourcesType = new ParameterizedTypeReference<>() {
		//don't care
	};

	@Test
	@DisplayName("Private resource are unreachable for unauthorized users")
	public void privateResourcesUnreachable() {
		client.get().uri("/resources").exchange().expectStatus().isUnauthorized().expectBody(ErrorDetails.class);
		authenticate(adminOnStudyEmail);
		client.get().uri("/resources").exchange().expectStatus().isOk();
	}

	@Test
	@DisplayName("Anyone can access public resources")
	public void publicResourcesReachable() {
		final var response = get("/resources/public", paginatedResourcesType);
		assertFalse(response.getObjects().isEmpty());
	}

	@Test
	@DisplayName("Create a private resource")
	public void createPrivateResource() {
		// Log in to create the resource
		authenticate(adminOnStudyEmail);

		final var root = scopeDAOService.getScopeByCode("Test");

		// Create the resource
		final var resourceDTO = createResourceDTO(root.getPk(), false);
		final var createdResource = post("/resources", resourceDTO, ResourceDTO.class);

		// The new resource is correct
		assertNotNull(createdResource.getPk());
		assertEquals("NEWSLETTERS", createdResource.getCategory().getId());

		// The newly created resource is present in the private resource list
		final var privateResources = get("/resources", paginatedResourcesType);
		assertTrue(
			privateResources.getObjects().stream()
				.anyMatch(resource -> resource.getPk().equals(createdResource.getPk()))
		);
	}

	@Test
	@DisplayName("Create a public resource")
	public void createPublicResource() {
		// Log in to create the resource
		authenticate(adminOnStudyEmail);

		final var root = scopeDAOService.getScopeByCode("Test");

		// Create a public resource
		final var resourceDTO = createResourceDTO(root.getPk(), true);
		final var createdResource = post("/resources", resourceDTO, ResourceDTO.class);

		// The new resource is correct
		assertNotNull(createdResource.getPk());
		assertEquals("NEWSLETTERS", createdResource.getCategory().getId());

		// The newly created resource is present in the private resource list
		final var privateResources = get("/resources", paginatedResourcesType);
		assertTrue(
			privateResources.getObjects().stream()
				.anyMatch(resource -> resource.getPk().equals(createdResource.getPk()))
		);

		// Log out and check if the newly created resource is in the public resources
		clearAuthentication();
		final var publicResources = get("/resources/public", paginatedResourcesType);
		assertTrue(
			publicResources.getObjects().stream()
				.anyMatch(resource -> resource.getPk().equals(createdResource.getPk()))
		);
	}

	@Test
	@DisplayName("Update resource")
	public void updateResource() {
		// Log in to create the resource
		authenticate(adminOnStudyEmail);

		final var root = scopeDAOService.getScopeByCode("Test");

		// Create a private resource
		final var resourceDTO = createResourceDTO(root.getPk(), false);
		final var createdResource = post("/resources", resourceDTO, ResourceDTO.class);

		// Modify the newly created resource
		final var newTitle = "New title";
		createdResource.setTitle(newTitle);
		createdResource.setCategoryId("DOCUMENTS");
		createdResource.setRemoved(true);
		createdResource.setPublicResource(true);

		// Update the resource on the server
		final var updatedResource = put("/resources/" + createdResource.getPk(), createdResource, ResourceDTO.class);

		// Check that the resource has been updated correctly
		assertEquals(newTitle, updatedResource.getTitle());
		assertEquals("DOCUMENTS", updatedResource.getCategory().getId());
		assertTrue(updatedResource.isPublicResource());

		// Check if the resource has become public
		clearAuthentication();
		final var publicResources = get("/resources/public", paginatedResourcesType);
		assertTrue(
			publicResources.getObjects().stream()
				.anyMatch(resource -> resource.getPk().equals(updatedResource.getPk()))
		);
	}

	private ResourceSubmissionDTO createResourceDTO(final Long scopePk, final boolean isPublic) {
		final var resourceDTO = new ResourceSubmissionDTO();
		resourceDTO.setPublicResource(isPublic);
		resourceDTO.setScopePk(scopePk);
		resourceDTO.setTitle("Quarterly report announcement");
		resourceDTO.setCategoryId("NEWSLETTERS");
		return resourceDTO;
	}
}
