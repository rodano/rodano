package ch.rodano.api.dataset;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Dataset submission")
public class DatasetSubmissionDTO {
	@Schema(description = "List of datasets to update")
	List<DatasetUpdateDTO> updatedDatasets;

	@Schema(description = "List of datasets to create")
	List<DatasetCreationDTO> newDatasets;

	@Schema(description = "Dictionary of dataset references to remove with a rationale")
	Map<Long, String> removedDatasets;

	@Schema(description = "List of datasets to restore with a rationale")
	List<DatasetRestorationDTO> restoredDatasets;

	public List<DatasetUpdateDTO> getUpdatedDatasets() {
		return updatedDatasets;
	}

	public void setUpdatedDatasets(final List<DatasetUpdateDTO> updatedDatasets) {
		this.updatedDatasets = updatedDatasets;
	}

	public List<DatasetCreationDTO> getNewDatasets() {
		return newDatasets;
	}

	public void setNewDatasets(final List<DatasetCreationDTO> newDatasets) {
		this.newDatasets = newDatasets;
	}

	public Map<Long, String> getRemovedDatasets() {
		return removedDatasets;
	}

	public void setRemovedDatasets(final Map<Long, String> removedDatasets) {
		this.removedDatasets = removedDatasets;
	}

	public List<DatasetRestorationDTO> getRestoredDatasets() {
		return restoredDatasets;
	}

	public void setRestoredDatasets(final List<DatasetRestorationDTO> restoredDatasets) {
		this.restoredDatasets = restoredDatasets;
	}
}
