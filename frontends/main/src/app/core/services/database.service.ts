import {HttpClient, HttpParams} from '@angular/common/http';
import {Service, inject} from '@angular/core';
import {APIService} from './api.service';
import {Bootstrap} from '../model/bootstrap';
import {Observable} from 'rxjs';
import {DemoUserScheme} from '../model/demo-user-scheme';
import {DenormalizationInconsistencyGroup} from '@core/model/denormalization-inconsistency-group';
import {ConfigurationInconsistencyGroup} from '@core/model/configuration-inconsistency-group';

@Service()
export class DatabaseService {
	private serviceUrl: string;

	private readonly http = inject(HttpClient);
	private readonly apiService = inject(APIService);

	constructor() {
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

	fixConfigConsistency(dryRun: boolean): Observable<ConfigurationInconsistencyGroup[]> {
		return this.http.post<ConfigurationInconsistencyGroup[]>(`${this.serviceUrl}/consistency/configuration`, {dryRun});
	}

	fixDenormalizationConsistency(dryRun: boolean): Observable<DenormalizationInconsistencyGroup[]> {
		return this.http.post<DenormalizationInconsistencyGroup[]>(`${this.serviceUrl}/consistency/denormalization`, {dryRun});
	}
}
