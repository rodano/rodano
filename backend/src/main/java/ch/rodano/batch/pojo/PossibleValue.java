package ch.rodano.batch.pojo;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PossibleValue {

	private String id;
	private Map<String, String> shortname;
	private String exportLabel;
	private Boolean specify;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Map<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final Map<String, String> shortname) {
		this.shortname = shortname;
	}

	public String getExportLabel() {
		return exportLabel;
	}

	public void setExportLabel(final String exportLabel) {
		this.exportLabel = exportLabel;
	}

	public Boolean getSpecify() {
		return specify;
	}

	public void setSpecify(final Boolean specify) {
		this.specify = specify;
	}

	@Override
	public String toString() {
		return "PossibleValue{" +
			"id='" + id + '\'' +
			", shortname=" + shortname +
			", exportLabel='" + exportLabel + '\'' +
			", specify=" + specify +
			'}';
	}
}
