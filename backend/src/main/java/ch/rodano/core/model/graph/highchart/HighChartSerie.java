package ch.rodano.core.model.graph.highchart;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRawValue;

@JsonInclude(Include.NON_NULL)
public class HighChartSerie {
	private String name;

	@JsonRawValue
	private String data;

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getData() {
		return data;
	}

	public void setData(final String data) {
		this.data = data;
	}
}
