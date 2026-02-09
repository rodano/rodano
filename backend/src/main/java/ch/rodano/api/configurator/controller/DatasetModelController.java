package ch.rodano.api.configurator.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.rodano.api.config.DatasetModelDTO;
import ch.rodano.core.aspects.SkipProjectAccessCheck;
import ch.rodano.core.services.bll.configurator.DatasetModelService;

@RestController
@RequestMapping("/superuser/configurator/projects/{projectId}/config")
@PreAuthorize("@userSecurityService.isSuperuser()")
public class DatasetModelController {

	private final DatasetModelService datasetModelService;

	public DatasetModelController(final DatasetModelService datasetModelService) {
		this.datasetModelService = datasetModelService;
	}

	/**
	 * Get all dataset models for a project
	 */
	@GetMapping("/dataset-models")
	public ResponseEntity<List<DatasetModelDTO>> getDatasetModels(@PathVariable final UUID projectId) {
		final var DatasetModels = datasetModelService.getDatasetModels(projectId);
		return ResponseEntity.ok(DatasetModels);
	}

	/**
	 * Get a specific dataset model
	 */
	@GetMapping("/dataset-models/{datasetModelId}")
	public ResponseEntity<DatasetModelDTO> getDatasetModel(
		@PathVariable final UUID projectId,
		@PathVariable final UUID datasetModelId
	) {
		final var DatasetModel = datasetModelService.getDatasetModel(projectId, datasetModelId);
		return ResponseEntity.ok(DatasetModel);
	}

	/**
	 * Create a new dataset model
	 */
	@PostMapping("/dataset-models")
	@SkipProjectAccessCheck
	public ResponseEntity<DatasetModelDTO> createDatasetModel(
		@PathVariable final UUID projectId,
		@RequestBody final DatasetModelDTO datasetModel
	) {
		final var created = datasetModelService.createDatasetModel(projectId, datasetModel);
		return ResponseEntity.ok(created);
	}

	/**
	 * Update an existing dataset model
	 */
	@PutMapping("/dataset-models/{datasetModelId}")
	@SkipProjectAccessCheck
	public ResponseEntity<DatasetModelDTO> updateDatasetModel(
		@PathVariable final UUID projectId,
		@PathVariable final UUID datasetModelId,
		@RequestBody final DatasetModelDTO datasetModel
	) {
		final var updated = datasetModelService.updateDatasetModel(projectId, datasetModelId, datasetModel);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Delete an dataset model
	 */
	@DeleteMapping("/dataset-models/{datasetModelId}")
	@SkipProjectAccessCheck
	public ResponseEntity<Void> deleteDatasetModel(
		@PathVariable final UUID projectId,
		@PathVariable final UUID datasetModelId
	) {
		datasetModelService.deleteDatasetModel(projectId, datasetModelId);
		return ResponseEntity.noContent().build();
	}
}
