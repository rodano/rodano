package ch.rodano.core.model.chart;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.api.config.ChartModelDTO;

//because of the generics, OpenAPI will generate a type ChartDTOObjectObject
@Schema(name = "ChartDTO")
public record ChartDTO<X, Y>(
	@Schema(description = "Chart model") @NotNull ChartModelDTO model,
	@Schema(description = "Chart model id") @NotBlank String modelId,
	@Schema(description = "Chart datasets") @NotNull List<ChartDatasetDTO<X, Y>> datasets
) {

	public ChartDTO(final ChartModelDTO model, final List<ChartDatasetDTO<X, Y>> datasets) {
		this(model, model.getId(), datasets);
	}
}
