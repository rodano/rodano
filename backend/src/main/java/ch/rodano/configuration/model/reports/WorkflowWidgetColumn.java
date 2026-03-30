package ch.rodano.configuration.model.reports;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.utils.DisplayableUtils;

public class WorkflowWidgetColumn implements Serializable, Node {
	@Serial
	private static final long serialVersionUID = 2737350294266317303L;

	private WorkflowWidget widget;

	private Map<String, String> label = new HashMap<>();
	private WorkflowWidgetColumnType type;

	@JsonBackReference
	public final WorkflowWidget getWidget() {
		return widget;
	}

	@JsonBackReference
	public final void setWidget(final WorkflowWidget widget) {
		this.widget = widget;
	}

	public Map<String, String> getLabel() {
		return label;
	}

	public void setLabel(final Map<String, String> label) {
		this.label = label;
	}

	public WorkflowWidgetColumnType getType() {
		return type;
	}

	public void setType(final WorkflowWidgetColumnType type) {
		this.type = type;
	}

	@JsonIgnore
	public String getLocalizedlabel(final String... languages) {
		return DisplayableUtils.getLocalizedMap(label, languages);
	}

	@Override
	@JsonIgnore
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}

	@Override
	public final Entity getEntity() {
		return Entity.WORKFLOW_WIDGET_COLUMN;
	}
}
