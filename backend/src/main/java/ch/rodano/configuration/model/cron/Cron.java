package ch.rodano.configuration.model.cron;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonBackReference;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.rules.Rule;
import ch.rodano.configuration.model.study.Study;

public class Cron implements Node {

	private static final long serialVersionUID = -4213624739903698733L;

	private String id;
	private Study study;

	private SortedMap<String, String> description;

	private Integer interval;
	private ChronoUnit intervalUnit;

	private List<Rule> rules;

	public Cron() {
		description = new TreeMap<>();
		rules = new ArrayList<>();
	}

	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	@JsonBackReference
	public final Study getStudy() {
		return study;
	}

	@JsonBackReference
	public final void setStudy(final Study study) {
		this.study = study;
	}

	public final SortedMap<String, String> getDescription() {
		return description;
	}

	public final void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	public final Integer getInterval() {
		return interval;
	}

	public final void setInterval(final Integer interval) {
		this.interval = interval;
	}

	public final ChronoUnit getIntervalUnit() {
		return intervalUnit;
	}

	public final void setIntervalUnit(final ChronoUnit intervalUnit) {
		this.intervalUnit = intervalUnit;
	}

	public final List<Rule> getRules() {
		return rules;
	}

	public final void setRules(final List<Rule> rules) {
		this.rules = rules;
	}

	@Override
	public Entity getEntity() {
		return Entity.CRON;
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
