import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {APIService} from './api.service';
import {Bootstrap} from '../model/bootstrap';
import {Observable} from 'rxjs';
import {DemoUserScheme} from '../model/demo-user-scheme';
import {DatabaseIssueGroup} from '@core/model/database-issue-group';

@Injectable({
	providedIn: 'root'
})
export class DatabaseService {
	private serviceUrl: string;

	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {
		this.serviceUrl = `${this.apiService.getApiUrl()}/administration/database`;
	}

	status(): Observable<{status: string}> {
		return this.http.get<{status: string}>(`${this.serviceUrl}/status`);
	}

	bootstrap(bootstrap: Bootstrap) {
		return this.http.post(`${this.serviceUrl}/bootstrap`, bootstrap);
	}

	createDemoUsers(scheme: DemoUserScheme) {
		return this.http.post(`${this.serviceUrl}/create-demo-users`, scheme);
	}

	generateRandomData(scale: number) {
		const params = new HttpParams()
			.set('scale', scale);
		return this.http.post(`${this.serviceUrl}/generate-random-data`, undefined, {params});
	}

	runDatabaseUpdate(dryRun: boolean): Observable<DatabaseIssueGroup[]> {
		return this.http.post<DatabaseIssueGroup[]>(`${this.serviceUrl}/update`, {dryRun});
	}
}
