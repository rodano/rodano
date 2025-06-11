package ch.rodano.core.model.layout.builder;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ch.rodano.configuration.model.layout.Layout;

public class LayoutGroupState {

	private final PageState pageState;
	private final Layout layout;

	private final List<LayoutState> layoutStates = new ArrayList<>();
	private boolean visible = true;

	public LayoutGroupState(final PageState pageState, final Layout layout) {
		this.pageState = pageState;
		this.layout = layout;
	}

	public PageState getPageState() {
		return pageState;
	}

	public final Layout getLayout() {
		return layout;
	}

	public void addLayoutState(final LayoutState layoutState) {
		layoutStates.add(layoutState);
	}

	public final List<LayoutState> getLayoutStates() {
		return layoutStates;
	}

	public void setVisible(final boolean visible) {
		this.visible = visible;
	}

	public final boolean isVisible() {
		return visible;
	}

	public final List<LayoutState> getLayoutStates(final Optional<ZonedDateTime> date) {
		//nothing to do is layout is not repeatable or if no date filter has been specified
		if(!layout.getType().isRepeatable() || date.isEmpty()) {
			return layoutStates;
		}

		//filter repeatable layouts using the creation date of their reference datasets
		return layoutStates.stream()
			.filter(layoutState -> {
				final var creationDate = layoutState.getReferenceDataset().getCreationTime();
				return creationDate.isBefore(date.get()) || creationDate.equals(date.get());
			}).toList();
	}

}
