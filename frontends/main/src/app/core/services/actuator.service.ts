import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {APIService} from './api.service';
import {Observable} from 'rxjs';
import {ScheduledTasks} from '../model/scheduled-tasks';
import {Info} from '../model/info';
import {Health} from '../model/health';
import {reviveDates} from '../decorators/revive-dates.decorator';

@Injectable({
	providedIn: 'root'
})
export class ActuatorService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {}

	getParameter(parameter: string): Observable<any> {
		return this.http.get<any>(`${this.apiService.getApiUrl()}/actuator/${parameter}`);
	}

	@reviveDates
	getInfo(): Observable<Info> {
		return this.http.get<any>(`${this.apiService.getApiUrl()}/actuator/info`);
	}

	getHealth(): Observable<Health> {
		return this.http.get<any>(`${this.apiService.getApiUrl()}/actuator/health`);
	}

	getScheduledTasks(): Observable<ScheduledTasks> {
		return this.http.get<any>(`${this.apiService.getApiUrl()}/actuator/scheduledtasks`);
	}
}
