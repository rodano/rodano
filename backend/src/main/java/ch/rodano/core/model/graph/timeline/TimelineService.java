package ch.rodano.core.model.graph.timeline;

import java.util.List;

import ch.rodano.configuration.model.study.Study;
import ch.rodano.configuration.model.timelinegraph.TimelineGraph;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.Scope;

public interface TimelineService {

	List<TimelineGraphData> getTimelineGraphs(Actor actor, List<Role> roles, Scope scope, Study study);

	TimelineGraphData generateTimelineGraphData(TimelineGraph config, Scope scope, String[] languages, Study study);
}
