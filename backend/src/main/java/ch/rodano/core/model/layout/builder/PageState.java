package ch.rodano.core.model.layout.builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ch.rodano.configuration.model.form.FormModel;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.scope.Scope;

public class PageState {
	final Scope scope;
	final Optional<Event> event;
	final FormModel formModel;
	final List<LayoutGroupState> layoutGroupStates = new ArrayList<>();

	public PageState(final Scope scope, final Optional<Event> event, final FormModel formModel) {
		this.scope = scope;
		this.event = event;
		this.formModel = formModel;
	}

	public Scope getScope() {
		return scope;
	}

	public Optional<Event> getVisit() {
		return event;
	}

	public FormModel getFormModel() {
		return formModel;
	}

	public void addLayoutGroupState(final LayoutGroupState layoutGroupState) {
		layoutGroupStates.add(layoutGroupState);
	}

	public List<LayoutGroupState> getLayoutGroupStates() {
		return layoutGroupStates;
	}

	public Optional<LayoutGroupState> getLayoutGroup(final String layoutId) {
		return layoutGroupStates.stream().filter(l -> l.getLayout().getId().equals(layoutId)).findFirst();
	}

	public Set<Dataset> getDatasets() {
		return layoutGroupStates.stream()
			.flatMap(l -> l.getLayoutStates().stream())
			.flatMap(l -> l.getDatasets().values().stream())
			.collect(Collectors.toCollection(HashSet::new));
	}
}
