package ch.rodano.configuration.model.cms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.utils.DisplayableUtils;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class CMSAction implements Node {
	private static final long serialVersionUID = -1200436924072696369L;

	private String id;
	private Map<String, String> labels;

	private String page;
	private List<String> context;
	private Map<String, String> parameters;

	private String section;

	public CMSAction() {
		labels = new TreeMap<>();
		context = new ArrayList<>();
		parameters = new HashMap<>();
	}

	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	public final Map<String, String> getLabels() {
		return labels;
	}

	public final void setLabels(final Map<String, String> labels) {
		this.labels = labels;
	}

	@JsonIgnore
	public final String getLocalizedLabel(final String... languages) {
		return DisplayableUtils.getLocalizedMap(labels, languages);
	}

	public final String getPage() {
		return page;
	}

	public final void setPage(final String page) {
		this.page = page;
	}

	public final List<String> getContext() {
		return context;
	}

	public final void setContext(final List<String> context) {
		this.context = context;
	}

	public final Map<String, String> getParameters() {
		return parameters;
	}

	public final void setParameters(final Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public final String getSection() {
		return section;
	}

	public final void setSection(final String section) {
		this.section = section;
	}

	@Override
	public final Entity getEntity() {
		return Entity.CMS_ACTION;
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
