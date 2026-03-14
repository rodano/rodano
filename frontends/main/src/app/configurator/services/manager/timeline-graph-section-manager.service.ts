import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {TimelineGraphSection} from '@core/model/timeline-graph-section';
import {TimelineGraphSectionService} from '../api/timeline-graph-section.service';
import {BaseManagerService} from './base-manager.service';

@Injectable({providedIn: 'root'})
export class TimelineGraphSectionManagerService extends BaseManagerService<TimelineGraphSection> {
	private currentTimelineGraphId: string | null = null;

	constructor(private timelineGraphSectionService: TimelineGraphSectionService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {
		return (s: TimelineGraphSection) => s.graphSectionId;
	}

	protected getSimpleFields(): (keyof TimelineGraphSection)[] {
		return ['id', 'type', 'timelineGraphId', 'datasetModelId', 'dateFieldId', 'endDateFieldId', 'labelFieldId', 'valueFieldId',
			'hideExpectedEvent', 'hideDoneEvent', 'useScopePaths', 'unit', 'color', 'strokeColor', 'opacity', 'dashed',
			'mark', 'positionStart', 'positionStop', 'scaleMin', 'scaleMax', 'scaleDecimal', 'scaleMarkInterval',
			'scaleLabelInterval', 'scalePosition', 'hiddenLegend', 'hidden'];
	}

	protected getTranslationFields(): (keyof TimelineGraphSection)[] {
		return ['label', 'tooltip'];
	}

	protected getArrayFields(): (keyof TimelineGraphSection)[] {
		return ['eventModelIds', 'metaFieldIds', 'references'];
	}

	loadForGraph(projectId: string, timelineGraphId: string): Observable<TimelineGraphSection[]> {
		this.currentTimelineGraphId = timelineGraphId;
		this.invalidate();
		return this.load(projectId);
	}

	protected fetchAll(projectId: string): Observable<TimelineGraphSection[]> {
		return this.timelineGraphSectionService.getSections(projectId, this.currentTimelineGraphId!);
	}

	protected createEntity(projectId: string, entity: TimelineGraphSection): Observable<TimelineGraphSection> {
		return this.timelineGraphSectionService.createSection(projectId, this.currentTimelineGraphId!, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.timelineGraphSectionService.deleteSection(projectId, this.currentTimelineGraphId!, id);
	}

	save(projectId: string, section: TimelineGraphSection): Observable<TimelineGraphSection> {
		return this.timelineGraphSectionService.updateSection(
			projectId,
			this.currentTimelineGraphId!,
			section.graphSectionId,
			section
		);
	}
}
