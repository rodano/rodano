package ch.rodano.api.config;

import java.util.List;
import java.util.Map;

public class MenuActionConfigDTO {

	private String page;
	private List<String> context;
	private Map<String, String> parameters;

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

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(final Map<String, String> parameters) {
		this.parameters = parameters;
	}
}
