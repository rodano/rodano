package ch.rodano.configuration.model.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.common.Node;
import ch.rodano.configuration.model.predicate.ValueSource;
import ch.rodano.configuration.model.study.Study;

public class Request implements Node {
	private static final long serialVersionUID = 7821582371613234362L;

	private Study study;

	protected List<ValueSource> results;
	protected List<ValueSource> parameters;
	protected ResultType resultType;

	public Request() {
		results = new ArrayList<>();
		parameters = new ArrayList<>();
	}

	@JsonIgnore
	public Study getStudy() {
		return study;
	}

	@JsonIgnore
	public void setStudy(final Study study) {
		this.study = study;
		getResults().forEach(vs -> vs.setStudy(this.study));
		getParameters().forEach(vs -> vs.setStudy(this.study));
	}

	public ResultType getResultType() {
		return resultType;
	}

	public void setResultType(final ResultType resultType) {
		this.resultType = resultType;
	}

	public List<ValueSource> getResults() {
		return results;
	}

	public void setResults(final List<ValueSource> results) {
		this.results = results;
		getResults().forEach(vs -> vs.setStudy(study));
	}

	public List<ValueSource> getParameters() {
		return parameters;
	}

	public void setParameters(final List<ValueSource> parameters) {
		this.parameters = parameters;
		getParameters().forEach(vs -> vs.setStudy(study));
	}

	@Override
	public Entity getEntity() {
		return Entity.CHART_REQUEST;
	}

	@Override
	public Collection<Node> getChildrenWithEntity(final Entity entity) {
		return Collections.emptyList();
	}
}
