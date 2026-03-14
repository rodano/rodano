import {TimelineGraphManagerService} from '../manager/timeline-graph-manager.service';
import {TimelineGraphSectionManagerService} from '../manager/timeline-graph-section-manager.service';
import {TimelineGraph} from '@core/model/timeline-graph';
import {TimelineGraphSection} from '@core/model/timeline-graph-section';

export interface TimelineGraphContext {
	timelineGraphManager: TimelineGraphManagerService;
	timelineGraphSectionManager: TimelineGraphSectionManagerService;
	timelineGraphs: TimelineGraph[];
	timelineGraphSections: TimelineGraphSection[];
	originalTimelineGraphs: TimelineGraph[];
	modifiedTimelineGraphIds: Set<string>;
	modifiedTimelineGraphSections: Set<string>;
}
