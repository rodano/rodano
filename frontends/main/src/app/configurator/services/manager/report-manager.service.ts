import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {ReportService} from '../api/report.service';
import {Report} from '@core/model/report';
import {BaseManagerService} from './base-manager.service';

@Injectable({providedIn: 'root'})
export class ReportManagerService extends BaseManagerService<Report> {
	constructor(private reportService: ReportService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {return (r: Report) => r.reportId;}
	protected getSimpleFields(): (keyof Report)[] {
		return ['id', 'workflowId', 'datasetModelId'];
	}

	protected getTranslationFields(): (keyof Report)[] {
		return ['shortname', 'longname', 'description'];
	}

	protected getArrayFields(): (keyof Report)[] {
		return ['fieldModelIds'];
	}

	protected fetchAll(projectId: string): Observable<Report[]> {
		return this.reportService.getReports(projectId);
	}

	protected createEntity(projectId: string, entity: Report): Observable<Report> {
		return this.reportService.createReport(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.reportService.deleteReport(projectId, id);
	}
}
