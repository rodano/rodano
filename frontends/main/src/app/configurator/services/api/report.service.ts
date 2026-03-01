import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Report} from '@core/model/report';

@Injectable({
	providedIn: 'root'
})
export class ReportService {
	constructor(private http: HttpClient) {}

	getReports(projectId: string): Observable<Report[]> {
		return this.http.get<Report[]>(`/api/superuser/configurator/projects/${projectId}/config/reports`);
	}

	getReport(projectId: string, reportId: string): Observable<Report> {
		return this.http.get<Report>(`/api/superuser/configurator/projects/${projectId}/config/reports/${reportId}`);
	}

	createReport(projectId: string, report: Report): Observable<Report> {
		return this.http.post<Report>(`/api/superuser/configurator/projects/${projectId}/config/reports`, report);
	}

	updateReport(projectId: string, reportId: string, report: Report): Observable<Report> {
		return this.http.put<Report>(`/api/superuser/configurator/projects/${projectId}/config/reports/${reportId}`, report);
	}

	deleteReport(projectId: string, reportId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/reports/${reportId}`);
	}
}
