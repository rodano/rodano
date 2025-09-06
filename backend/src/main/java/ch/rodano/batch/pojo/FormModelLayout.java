package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FormModelLayout {

	private String id;
	private Map<String, String> description;
	private Map<String, String> textBefore;
	private Map<String, String> textAfter;

	private String type;
	private String cssCode;

	private String datasetModelId;
	private String defaultSortFieldModelId;

	private List<FormModelLayoutColumn> columns;
	private List<FormModelLayoutLine> lines;

	private RuleConstraint constraint;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Map<String, String> getDescription() {
		return description;
	}

	public void setDescription(final Map<String, String> description) {
		this.description = description;
	}

	public Map<String, String> getTextBefore() {
		return textBefore;
	}

	public void setTextBefore(final Map<String, String> textBefore) {
		this.textBefore = textBefore;
	}

	public Map<String, String> getTextAfter() {
		return textAfter;
	}

	public void setTextAfter(final Map<String, String> textAfter) {
		this.textAfter = textAfter;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public String getCssCode() {
		return cssCode;
	}

	public void setCssCode(final String cssCode) {
		this.cssCode = cssCode;
	}

	public String getDatasetModelId() {
		return datasetModelId;
	}

	public void setDatasetModelId(final String datasetModelId) {
		this.datasetModelId = datasetModelId;
	}

	public String getDefaultSortFieldModelId() {
		return defaultSortFieldModelId;
	}

	public void setDefaultSortFieldModelId(final String defaultSortFieldModelId) {
		this.defaultSortFieldModelId = defaultSortFieldModelId;
	}

	public List<FormModelLayoutColumn> getColumns() {
		return columns;
	}

	public void setColumns(final List<FormModelLayoutColumn> columns) {
		this.columns = columns;
	}

	public List<FormModelLayoutLine> getLines() {
		return lines;
	}

	public void setLines(final List<FormModelLayoutLine> lines) {
		this.lines = lines;
	}

	public RuleConstraint getConstraint() {
		return constraint;
	}

	public void setConstraint(final RuleConstraint constraint) {
		this.constraint = constraint;
	}

	@Override
	public String toString() {
		return "FormModelLayout{" +
			"id='" + id + '\'' +
			", description=" + description +
			", textBefore=" + textBefore +
			", textAfter=" + textAfter +
			", type='" + type + '\'' +
			", cssCode='" + cssCode + '\'' +
			", datasetModelId='" + datasetModelId + '\'' +
			", defaultSortFieldModelId='" + defaultSortFieldModelId + '\'' +
			", columns=" + columns +
			", lines=" + lines +
			", constraint=" + constraint +
			'}';
	}
}
