import {Injectable} from '@angular/core';
import {EntityModificationTracker} from '../entity-modification-tracker';
import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {ChartModel} from '@core/model/chart-model';
import {ChartService} from '../api/chart.service';

@Injectable({
	providedIn: 'root'
})
export class ChartManagerService {
	private tracker: EntityModificationTracker<ChartModel>;
	private loaded = false;
	private fullLoaded = false;

	constructor(private chartService: ChartService) {
		this.tracker = new EntityModificationTracker<ChartModel>(
			chart => chart.chartId,
			[
				'id', 'type', 'overrideUserRights', 'withStatistics', 'displayExpected', 'chartId', 'scopeModelId',
				'leafScopeModelId', 'datasetModelId', 'fieldModelId', 'workflowId'
			],
			['shortname', 'longname', 'description', 'title', 'legendX', 'legendY'],
			['colors', 'ranges', 'stateFilters']
		);
	}

	load(projectId: string): Observable<ChartModel[]> {
		if(this.loaded) {
			return of(this.tracker.getCurrent());
		}
		return this.chartService.getCharts(projectId).pipe(
			map(models => {
				this.tracker.initialize(models);
				this.loaded = true;
				return models;
			})
		);
	}

	loadFull(projectId: string): Observable<ChartModel[]> {
		if(this.fullLoaded) {
			return of(this.tracker.getCurrent());
		}

		return this.chartService.getChartsFull(projectId).pipe(
			map(fullModels => {
				fullModels.forEach(fullModel => {
					const existing = this.tracker.getEntity(fullModel.chartId);
					if(existing) {
						Object.assign(existing, fullModel);
					}
					else {
						this.tracker.addEntity(fullModel);
					}
				});
				this.loaded = true;
				this.fullLoaded = true;
				this.tracker.syncOriginalsWithCurrent();
				return this.tracker.getCurrent();
			})
		);
	}

	invalidate(): void {
		this.loaded = false;
		this.fullLoaded = false;
	}

	create(projectId: string, chart: ChartModel): Observable<ChartModel> {
		return this.chartService.createChart(projectId, chart).pipe(
			map(created => {
				this.tracker.addEntity(created);
				return created;
			})
		);
	}

	update(chart: ChartModel): void {
		this.tracker.updateEntity(chart);
	}

	delete(projectId: string, chartId: string): Observable<void> {
		return this.chartService.deleteChart(projectId, chartId).pipe(
			map(() => {
				this.tracker.removeEntity(chartId);
			})
		);
	}

	getModifiedIds(): Set<string> {
		return this.tracker.getModifiedIds();
	}

	getModifiedFieldsMap(): Map<string, Set<string>> {
		return this.tracker.getModifiedFieldsMap();
	}

	getOriginals(): ChartModel[] {
		return this.tracker.getOriginals();
	}

	clearModifications(): void {
		this.tracker.clearModifications();
	}

	resetToOriginals(): void {
		this.tracker.resetToOriginals();
	}

	getAll(): ChartModel[] {
		return this.tracker.getCurrent();
	}

	getById(id: string): ChartModel | undefined {
		return this.tracker.getEntity(id);
	}

	isModified(id: string): boolean {
		return this.tracker.isModified(id);
	}

	isFieldModified(chartId: string, fieldName: string): boolean {
		return this.tracker.isFieldModified(chartId, fieldName);
	}

	getModificationCount(): number {
		return this.tracker.getTotalModifiedFieldsCount();
	}

	syncOriginalsWithCurrent(): void {
		this.tracker.syncOriginalsWithCurrent();
	}
}
