package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuAction {

	private Map<String, String> labels;
	private String page;
	private List<String> context;
	private Map<String, Object> parameters;

	public Map<String, String> getLabels() {
		return labels;
	}

	public void setLabels(final Map<String, String> labels) {
		this.labels = labels;
	}

	public String getPage() {
		return page;
	}

	public void setPage(final String page) {
		this.page = page;
	}

	public List<String> getContext() {
		return context;
	}

	public void setContext(final List<String> context) {
		this.context = context;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(final Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		return "MenuAction{" +
			"labels=" + labels +
			", page='" + page + '\'' +
			", context=" + context +
			", parameters=" + parameters +
			'}';
	}
}
