import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { APIService } from './api.service';
import { map } from 'rxjs/operators';
import { Observable } from 'rxjs';

@Injectable()
export class AdministrationService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {}

	reloadConfiguration(): Observable<void> {
		return this.http.post<void>(`${this.apiService.getApiUrl()}/administration/reload`, undefined);
	}

	isInMaintenance(): Observable<boolean> {
		return this.http.get<{data: boolean}>(`${this.apiService.getApiUrl()}/administration/in-maintenance`).pipe(
			map(result => result.data)
		);
	}

	setMaintenance(state: boolean) {
		return this.http.post(`${this.apiService.getApiUrl()}/administration/maintenance`, {state});
	}

	isInDebug(): Observable<boolean> {
		return this.http.get<{data: boolean}>(`${this.apiService.getApiUrl()}/administration/in-debug`).pipe(
			map(result => result.data)
		);
	}

	setDebug(state: boolean) {
		return this.http.post(`${this.apiService.getApiUrl()}/administration/debug`, {state});
	}

}
