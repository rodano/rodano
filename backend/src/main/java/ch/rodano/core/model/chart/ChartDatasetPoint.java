package ch.rodano.core.model.chart;

import java.time.ZonedDateTime;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChartDatasetPoint<X, Y>(
	@NotBlank @Schema(oneOf = {
		String.class, ZonedDateTime.class }) X x,
	@NotBlank @Schema(oneOf = {
		String.class, Integer.class }) Y y
){
}
