import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Cron} from '@core/model/cron';

@Injectable({
	providedIn: 'root'
})
export class CronService {
	constructor(private http: HttpClient) {}

	getCrons(projectId: string): Observable<Cron[]> {
		return this.http.get<Cron[]>(`/api/superuser/configurator/projects/${projectId}/config/crons`);
	}

	getCron(projectId: string, cronId: string): Observable<Cron> {
		return this.http.get<Cron>(`/api/superuser/configurator/projects/${projectId}/config/crons/${cronId}`);
	}

	createCron(projectId: string, cron: Cron): Observable<Cron> {
		return this.http.post<Cron>(`/api/superuser/configurator/projects/${projectId}/config/crons`, cron);
	}

	updateCron(projectId: string, cronId: string, cron: Cron): Observable<Cron> {
		return this.http.put<Cron>(`/api/superuser/configurator/projects/${projectId}/config/crons/${cronId}`, cron);
	}

	deleteCron(projectId: string, cronId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/crons/${cronId}`);
	}
}
