package ch.rodano.configuration.model.cms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.utils.DisplayableUtils;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class CMSSection implements Node, Comparable<CMSSection>, Cloneable {
	private static final long serialVersionUID = -8381830978095030796L;

	private CMSLayout layout;

	protected String id;
	protected Map<String, String> labels;

	protected String requiredFeature;
	protected ScopeCriterionRight requiredRight;
	protected List<String> requiredScopeIds;

	protected List<CMSWidget> widgets;

	public CMSSection() {
		labels = new TreeMap<>();
		requiredScopeIds = new ArrayList<>();
		widgets = new ArrayList<>();
	}

	public final CMSLayout getLayout() {
		return layout;
	}

	public final void setLayout(final CMSLayout layout) {
		this.layout = layout;
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
	public final void addLabel(final String language, final String value) {
		labels.put(language, value);
	}

	@JsonIgnore
	public final String getLocalizedLabel(final String... languages) {
		return DisplayableUtils.getLocalizedMap(labels, languages);
	}

	public final String getRequiredFeature() {
		return requiredFeature;
	}

	public final void setRequiredFeature(final String requiredFeature) {
		this.requiredFeature = requiredFeature;
	}

	public final ScopeCriterionRight getRequiredRight() {
		return requiredRight;
	}

	public final void setRequiredRight(final ScopeCriterionRight requiredRight) {
		this.requiredRight = requiredRight;
	}

	public final List<String> getRequiredScopeIds() {
		return requiredScopeIds;
	}

	public final void setRequiredScopeIds(final List<String> requiredScopeIds) {
		this.requiredScopeIds = requiredScopeIds;
	}

	public final void setWidgets(final List<CMSWidget> widgets) {
		this.widgets = widgets;
	}

	public final List<CMSWidget> getWidgets() {
		return widgets;
	}

	public final void addWidget(final CMSWidget widget) {
		widgets.add(widget);
	}

	@Override
	public final Entity getEntity() {
		return Entity.CMS_SECTION;
	}

	@JsonIgnore
	@Override
	public final int compareTo(final CMSSection o) {
		if(equals(o)) {
			return 0;
		}
		if(getLayout() == null) {
			return 1;
		}
		if(o.getLayout() == null) {
			return -1;
		}
		if(!getLayout().equals(o.getLayout())) {
			return getId().compareTo(o.getId());
		}
		return getLayout().getSections().indexOf(this) - getLayout().getSections().indexOf(o);
	}

	//it may exist multiple instances of the same layout because layout are often de-serialized from json files
	@Override
	public boolean equals(final Object obj) {
		if(obj == null) {
			return false;
		}
		if(obj == this) {
			return true;
		}
		if(obj.getClass() != getClass()) {
			return false;
		}
		final var section = (CMSSection) obj;
		return new EqualsBuilder()
			.append(id, section.id)
			.append(widgets, section.widgets)
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(id)
			.append(widgets)
			.hashCode();
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
