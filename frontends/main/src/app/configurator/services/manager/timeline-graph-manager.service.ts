import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {TimelineGraph} from '@core/model/timeline-graph';
import {TimelineGraphService} from '../api/timeline-graph.service';
import {BaseManagerService} from './base-manager.service';

@Injectable({providedIn: 'root'})
export class TimelineGraphManagerService extends BaseManagerService<TimelineGraph> {
	constructor(private timelineGraphService: TimelineGraphService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {
		return (tg: TimelineGraph) => tg.timelineGraphId;
	}

	protected getSimpleFields(): (keyof TimelineGraph)[] {
		return ['id', 'scopeModelId', 'studyStartEventModelId', 'studyEndEventModelId', 'studyPeriodIsDefault',
			'width', 'height', 'legendWidth', 'scrollerHeight', 'showScroller'];
	}

	protected getTranslationFields(): (keyof TimelineGraph)[] {
		return ['shortname', 'longname', 'description', 'footnote'];
	}

	protected getArrayFields(): (keyof TimelineGraph)[] {
		return ['sections'];
	}

	protected fetchAll(projectId: string): Observable<TimelineGraph[]> {
		return this.timelineGraphService.getTimelineGraphs(projectId);
	}

	protected createEntity(projectId: string, entity: TimelineGraph): Observable<TimelineGraph> {
		return this.timelineGraphService.createTimelineGraph(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.timelineGraphService.deleteTimelineGraph(projectId, id);
	}
}
