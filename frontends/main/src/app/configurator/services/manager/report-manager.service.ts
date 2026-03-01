import {Injectable} from '@angular/core';
import {EntityModificationTracker} from '../entity-modification-tracker';
import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {ReportService} from '../api/report.service';
import {Report} from '@core/model/report';

@Injectable({providedIn: 'root'})
export class ReportManagerService {
	private tracker: EntityModificationTracker<Report>;
	private loaded = false;

	constructor(private reportService: ReportService) {
		this.tracker = new EntityModificationTracker<Report>(
			report => report.reportId,
			['id', 'workflowId', 'datasetModelId'],
			['shortname', 'longname', 'description'],
			['fieldModelIds']
		);
	}

	load(projectId: string): Observable<Report[]> {
		if(this.loaded) {
			return of(this.tracker.getCurrent());
		}
		return this.reportService.getReports(projectId).pipe(
			map(reports => {
				this.tracker.initialize(reports);
				this.loaded = true;
				return reports;
			})
		);
	}

	invalidate(): void {
		this.loaded = false;
	}

	create(projectId: string, report: Report): Observable<Report> {
		return this.reportService.createReport(projectId, report).pipe(
			map(created => {
				this.tracker.addEntity(created);
				return created;
			})
		);
	}

	update(report: Report): void {
		this.tracker.updateEntity(report);
	}

	delete(projectId: string, reportId: string): Observable<void> {
		return this.reportService.deleteReport(projectId, reportId).pipe(
			map(() => {
				this.tracker.removeEntity(reportId);
			})
		);
	}

	getModifiedIds(): Set<string> {
		return this.tracker.getModifiedIds();
	}

	getModifiedFieldsMap(): Map<string, Set<string>> {
		return this.tracker.getModifiedFieldsMap();
	}

	getOriginals(): Report[] {
		return this.tracker.getOriginals();
	}

	clearModifications(): void {
		this.tracker.clearModifications();
	}

	resetToOriginals(): void {
		this.tracker.resetToOriginals();
	}

	getAll(): Report[] {
		return this.tracker.getCurrent();
	}

	getById(id: string): Report | undefined {
		return this.tracker.getEntity(id);
	}

	isModified(reportId: string): boolean {
		return this.tracker.isModified(reportId);
	}

	isFieldModified(reportId: string, fieldName: string): boolean {
		return this.tracker.isFieldModified(reportId, fieldName);
	}

	getModificationCount(): number {
		return this.tracker.getTotalModifiedFieldsCount();
	}

	updateOnServer(projectId: string, reportId: string, report: Report): Observable<Report> {
		return this.reportService.updateReport(projectId, reportId, report);
	}

	syncOriginalsWithCurrent(): void {
		this.tracker.syncOriginalsWithCurrent();
	}
}
