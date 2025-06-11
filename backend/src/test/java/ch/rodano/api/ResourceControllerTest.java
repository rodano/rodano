package ch.rodano.api;

import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.exception.ErrorDetails;
import ch.rodano.api.resource.ResourceDTO;
import ch.rodano.api.resource.ResourceSubmissionDTO;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.test.ControllerTest;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
class ResourceControllerTest extends ControllerTest {

	@Autowired
	private ScopeDAOService scopeDAOService;

	final ParameterizedTypeReference<PagedResult<ResourceDTO>> type = new ParameterizedTypeReference<>() {};

	@Test
	@DisplayName("Private resource are unreachable for unauthorized users")
	public void privateResourcesUnreachable() {
		final var unauthorizedResponse = restTemplate.getForEntity("/resources", ErrorDetails.class);
		assertEquals(HttpStatus.UNAUTHORIZED, unauthorizedResponse.getStatusCode());

		authenticate(adminOnStudyEmail);
		final var authorizedResponse = executeGet("/resources", type);
		assertEquals(HttpStatus.OK, authorizedResponse.getStatusCode());
	}

	@Test
	@DisplayName("Anyone can access public resources")
	public void publicResourcesReachable() {
		final var response = executeGet("/resources/public", type);
		assertTrue(response.getBody().getObjects().size() > 0);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	@DisplayName("Create a private resource")
	public void createPrivateResource() {
		// Log in to create the resource
		authenticate(adminOnStudyEmail);

		final var root = scopeDAOService.getScopeByCode("Test");

		// Create the resource
		final var resourceDTO = createResourceDTO(root.getPk(), false);
		final var resourceEntity = new HttpEntity<>(resourceDTO);
		final var createdResource = executePostAndReturnBody("/resources", resourceEntity, ResourceDTO.class);

		// The new resource is correct
		assertNotNull(createdResource.getPk());
		assertEquals("NEWSLETTERS", createdResource.getCategory().getId());

		// The newly created resource is present in the private resource list
		final var privateResources = executeGetAndReturnBody("/resources", type);
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
		final var resourceEntity = new HttpEntity<>(resourceDTO);
		final var createdResource = executePostAndReturnBody("/resources", resourceEntity, ResourceDTO.class);

		// The new resource is correct
		assertNotNull(createdResource.getPk());
		assertEquals("NEWSLETTERS", createdResource.getCategory().getId());

		// The newly created resource is present in the private resource list
		final var privateResources = executeGetAndReturnBody("/resources", type);
		assertTrue(
			privateResources.getObjects().stream()
				.anyMatch(resource -> resource.getPk().equals(createdResource.getPk()))
		);

		// Log out and check if the newly created resource is in the public resources
		clearAuthentication();
		final var publicResources = executeGetAndReturnBody("/resources/public", type);
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
		final var resourceEntity = new HttpEntity<>(resourceDTO);
		final var createdResource = executePostAndReturnBody("/resources", resourceEntity, ResourceDTO.class);

		// Modify the newly created resource
		final var newTitle = "New title";
		createdResource.setTitle(newTitle);
		createdResource.setCategoryId("DOCUMENTS");
		createdResource.setRemoved(true);
		createdResource.setPublicResource(true);

		// Update the resource on the server
		final var updateEntity = new HttpEntity<>(createdResource);
		final var updatedResourceEntity = restTemplate.exchange(
			"/resources/{resourcePk}",
			HttpMethod.PUT,
			updateEntity,
			ResourceDTO.class,
			Collections.singletonMap("resourcePk", createdResource.getPk())
		);
		final var updatedResource = updatedResourceEntity.getBody();

		// Check that the resource has been updated correctly
		assertEquals(newTitle, updatedResource.getTitle());
		assertEquals("DOCUMENTS", updatedResource.getCategory().getId());
		assertTrue(updatedResource.isPublicResource());

		// Check if the resource has become public
		clearAuthentication();
		final var publicResources = executeGetAndReturnBody("/resources/public", type);
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
