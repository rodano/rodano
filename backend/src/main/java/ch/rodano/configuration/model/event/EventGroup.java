package ch.rodano.configuration.model.event;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.study.Study;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class EventGroup implements Serializable, SuperDisplayable, Node, Comparable<EventGroup> {
	private static final long serialVersionUID = -5410921258509148168L;

	private static Comparator<EventGroup> DEFAULT_COMPARATOR = Comparator
		.comparing(EventGroup::getOrderBy)
		.thenComparing(EventGroup::getId);

	private ScopeModel scopeModel;

	private String id;
	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	private int orderBy;
	private String icon;

	public EventGroup() {
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
	}

	@JsonBackReference
	public void setScopeModel(final ScopeModel scopeModel) {
		this.scopeModel = scopeModel;
	}

	@JsonBackReference
	public final ScopeModel getScopeModel() {
		return scopeModel;
	}

	@JsonIgnore
	public Study getStudy() {
		return scopeModel.getStudy();
	}

	@Override
	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	public final int getOrderBy() {
		return orderBy;
	}

	public final void setOrderBy(final int orderBy) {
		this.orderBy = orderBy;
	}

	public final String getIcon() {
		return icon;
	}

	public final void setIcon(final String icon) {
		this.icon = icon;
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

	@JsonIgnore
	public final List<EventModel> getEventModels() {
		return scopeModel.getEventModels().stream().filter(e -> e.getEventGroupId().equals(id)).toList();
	}

	@Override
	public final Entity getEntity() {
		return Entity.EVENT_GROUP;
	}

	@Override
	@JsonIgnore
	public final Collection<Node> getChildrenWithEntity(final Entity entity) {
		switch(entity) {
			case EVENT_MODEL:
				return Collections.unmodifiableList(getEventModels());
			default:
				return Collections.emptyList();
		}
	}

	@Override
	public final int compareTo(final EventGroup otherEventGroup) {
		return DEFAULT_COMPARATOR.compare(this, otherEventGroup);
	}
}
