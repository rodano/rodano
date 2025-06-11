import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {APIService} from './api.service';
import {map, shareReplay} from 'rxjs/operators';
import {Observable} from 'rxjs';

@Injectable({
	providedIn: 'root'
})
export class AdministrationService {
	private serviceUrl: string;
	private isInDebug$: Observable<boolean>;

	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {
		this.serviceUrl = `${this.apiService.getApiUrl()}/administration`;
		//cache debug mode
		//TODO this is not optimal (when the cache is reset)
		//a real subject should be used, maybe created in another service
		this.resetIsInDebug();
	}

	reloadConfiguration(): Observable<void> {
		return this.http.post<void>(`${this.serviceUrl}/reload`, undefined);
	}

	isInMaintenance(): Observable<boolean> {
		return this.http.get<{state: boolean}>(`${this.serviceUrl}/maintenance`).pipe(
			map(result => result.state)
		);
	}

	setMaintenance(state: boolean) {
		return this.http.post(`${this.serviceUrl}/maintenance`, {state});
	}

	private resetIsInDebug() {
		this.isInDebug$ = this.http.get<{state: boolean}>(`${this.serviceUrl}/debug`).pipe(
			shareReplay(),
			map(result => result.state)
		);
	}

	isInDebug(): Observable<boolean> {
		return this.isInDebug$;
	}

	setDebug(state: boolean) {
		this.resetIsInDebug();
		return this.http.post(`${this.serviceUrl}/debug`, {state});
	}

	executeScheduledTask(scheduledTaskName: string) {
		const params = new HttpParams()
			.set('scheduledTaskName', scheduledTaskName);
		return this.http.put(`${this.serviceUrl}/execute-scheduled-task`, undefined, {params});
	}
}
