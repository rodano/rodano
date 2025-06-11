package ch.rodano.configuration.model.reports;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.common.SuperDisplayable;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class WorkflowWidgetColumn implements SuperDisplayable, Serializable, Node, Comparable<WorkflowWidgetColumn> {
	private static final long serialVersionUID = 2737350294266317303L;

	private WorkflowWidget widget;

	private String id;
	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	private WorkflowWidgetColumnType type;
	private int width;

	public WorkflowWidgetColumn() {
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
	}

	@JsonBackReference
	public final WorkflowWidget getWidget() {
		return widget;
	}

	@JsonBackReference
	public final void setWidget(final WorkflowWidget widget) {
		this.widget = widget;
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@Override
	public final SortedMap<String, String> getShortname() {
		return shortname;
	}

	public final void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	@Override
	public final SortedMap<String, String> getLongname() {
		return longname;
	}

	public final void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	@Override
	public final SortedMap<String, String> getDescription() {
		return description;
	}

	public final void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	public WorkflowWidgetColumnType getType() {
		return type;
	}

	public void setType(final WorkflowWidgetColumnType type) {
		this.type = type;
	}

	@JsonIgnore
	public final String getIdForExtJs() {
		return id.replaceAll("\\.", "");
	}

	public final int getWidth() {
		return width;
	}

	public final void setWidth(final int width) {
		this.width = width;
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

	@Override
	public final int compareTo(final WorkflowWidgetColumn o) {
		return getWidget().getColumns().indexOf(this) - o.getWidget().getColumns().indexOf(o);
	}
}
