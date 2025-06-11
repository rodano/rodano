import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {APIService} from './api.service';
import {BootstrapDTO} from '../model/bootstrap-dto';
import {Observable} from 'rxjs';
import {DemoUserSchemeDTO} from '../model/demo-user-scheme-dto';

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

	bootstrap(bootstrap: BootstrapDTO) {
		return this.http.post(`${this.serviceUrl}/bootstrap`, bootstrap);
	}

	createDemoUsers(scheme: DemoUserSchemeDTO) {
		return this.http.post(`${this.serviceUrl}/create-demo-users`, scheme);
	}

	generateRandomData(scale: number) {
		const params = new HttpParams()
			.set('scale', scale);
		return this.http.post(`${this.serviceUrl}/generate-random-data`, undefined, {params});
	}
}
