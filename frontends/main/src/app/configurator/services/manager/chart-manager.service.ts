import {Injectable} from '@angular/core';
import {Observable, of} from 'rxjs';
import {ChartModel} from '@core/model/chart-model';
import {ChartService} from '../api/chart.service';
import {BaseManagerService} from './base-manager.service';
import {map} from 'rxjs/operators';

@Injectable({providedIn: 'root'})
export class ChartManagerService extends BaseManagerService<ChartModel> {
	private fullLoaded = false;

	constructor(private chartService: ChartService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {return (c: ChartModel) => c.chartId;}
	protected getSimpleFields(): (keyof ChartModel)[] {
		return ['id', 'type', 'overrideUserRights', 'withStatistics', 'displayExpected', 'chartId', 'scopeModelId',
			'leafScopeModelId', 'datasetModelId', 'fieldModelId', 'workflowId'];
	}

	protected getTranslationFields(): (keyof ChartModel)[] {
		return ['shortname', 'longname', 'description', 'title', 'legendX', 'legendY'];
	}

	protected getArrayFields(): (keyof ChartModel)[] {
		return ['colors', 'ranges', 'stateFilters'];
	}

	protected fetchAll(projectId: string): Observable<ChartModel[]> {
		return this.chartService.getCharts(projectId);
	}

	protected createEntity(projectId: string, entity: ChartModel): Observable<ChartModel> {
		return this.chartService.createChart(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.chartService.deleteChart(projectId, id);
	}

	override invalidate(): void {
		super.invalidate();
		this.fullLoaded = false;
	}

	loadFull(projectId: string): Observable<ChartModel[]> {
		if(this.fullLoaded) {
			return of(this.tracker.getCurrent());
		}
		return this.chartService.getChartsFull(projectId).pipe(
			map(fullModels => {
				fullModels.forEach(fullModel => {
					const existing = this.getById(fullModel.chartId);
					if(existing) {
						Object.assign(existing, fullModel);
					}
					else {
						this.tracker.addEntity(fullModel);
					}
				});
				this.loaded = true;
				this.fullLoaded = true;
				this.syncOriginalsWithCurrent();
				return this.getAll();
			})
		);
	}
}
