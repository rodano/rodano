package ch.rodano.configuration.model.validator;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.common.SuperDisplayable;
import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.configuration.model.rules.RuleConstraint;
import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.model.workflow.Workflow;
import ch.rodano.configuration.model.workflow.WorkflowState;
import ch.rodano.configuration.utils.DisplayableUtils;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class Validator implements Serializable, SuperDisplayable, Node, Comparable<Validator> {
	private static final long serialVersionUID = -7922512122647649002L;

	public static final Map<String, String> REQUIRED = Map.of(
		LanguageStatic.en.name(), "is required",
		LanguageStatic.fr.name(), "est requis"
		);

	private static Comparator<Validator> DEFAULT_COMPARATOR = Comparator.comparing(Validator::getId);

	public static final Comparator<Validator> COMPARATOR_IMPORTANCE = (v1, v2) -> {
		if(v1 == v2) {
			return 0;
		}

		//required validators come first
		if(v1.required && !v2.required) {
			return -1;
		}
		if(!v1.required && v2.required) {
			return 1;
		}

		//blocking validator comes first
		if(v1.isBlocking() && !v2.isBlocking()) {
			return -1;
		}
		if(!v1.isBlocking() && v2.isBlocking()) {
			return -1;
		}

		return DEFAULT_COMPARATOR.compare(v1, v2);
	};

	private String id;
	private Study study;

	private SortedMap<String, String> shortname;
	private SortedMap<String, String> longname;
	private SortedMap<String, String> description;

	private boolean required;
	private boolean script;

	private String workflowId;
	private String invalidStateId;
	private String validStateId;

	private SortedMap<String, String> message;
	private RuleConstraint constraint;

	public Validator() {
		shortname = new TreeMap<>();
		longname = new TreeMap<>();
		description = new TreeMap<>();
		message = new TreeMap<>();
	}

	public final void setId(final String id) {
		this.id = id;
	}

	@Override
	public final String getId() {
		return id;
	}

	@JsonBackReference
	public final void setStudy(final Study study) {
		this.study = study;
	}

	@JsonBackReference
	public final Study getStudy() {
		return study;
	}

	public final boolean isRequired() {
		return required;
	}

	public final void setRequired(final boolean required) {
		this.required = required;
	}

	public final boolean isScript() {
		return script;
	}

	public final void setScript(final boolean script) {
		this.script = script;
	}

	public final RuleConstraint getConstraint() {
		return constraint;
	}

	public final void setConstraint(final RuleConstraint constraint) {
		this.constraint = constraint;
	}

	public final String getWorkflowId() {
		return workflowId;
	}

	public final void setWorkflowId(final String workflowId) {
		this.workflowId = workflowId;
	}

	public String getInvalidStateId() {
		return invalidStateId;
	}

	public void setInvalidStateId(final String invalidStateId) {
		this.invalidStateId = invalidStateId;
	}

	public String getValidStateId() {
		return validStateId;
	}

	public void setValidStateId(final String validStateId) {
		this.validStateId = validStateId;
	}

	public final SortedMap<String, String> getMessage() {
		return message;
	}

	public final void setMessage(final SortedMap<String, String> message) {
		this.message = message;
	}

	@Override
	public final SortedMap<String, String> getDescription() {
		return description;
	}

	public final void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	@Override
	public final SortedMap<String, String> getLongname() {
		return longname;
	}

	public void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	@Override
	public final SortedMap<String, String> getShortname() {
		return shortname;
	}

	public final void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	public final String getLocalizedMessage(final String... languages) {
		return DisplayableUtils.getLocalizedMap(message, languages);
	}

	@JsonIgnore
	public String getDefaultLocalizedMessage() {
		return getLocalizedMessage(getStudy().getDefaultLanguage().getId());
	}

	@JsonIgnore
	public final boolean isBlocking() {
		return StringUtils.isBlank(workflowId);
	}

	@JsonIgnore
	public final boolean hasMessage() {
		return MapUtils.isNotEmpty(message) && message.values().stream().anyMatch(StringUtils::isNotBlank);
	}

	@JsonIgnore
	public final Workflow getWorkflow() {
		return getStudy().getWorkflow(getWorkflowId());
	}

	@JsonIgnore
	public final WorkflowState getInvalidWorkflowState() {
		return getWorkflow().getState(invalidStateId);
	}

	@JsonIgnore
	public final WorkflowState getValidWorkflowState() {
		return getWorkflow().getState(validStateId);
	}

	@Override
	public final Entity getEntity() {
		return Entity.VALIDATOR;
	}

	@Override
	@JsonIgnore
	public final Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}

	@Override
	@JsonIgnore
	public int compareTo(final Validator otherValidator) {
		return COMPARATOR_IMPORTANCE.compare(this, otherValidator);
	}
}
