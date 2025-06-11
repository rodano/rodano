import {HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {APIService} from './api.service';

@Injectable({
	providedIn: 'root'
})
export class ReportService {
	private serviceUrl: string;

	constructor(
		private apiService: APIService
	) {
		this.serviceUrl = `${this.apiService.getApiUrl()}/reports`;
	}

	getScopeTransfersUrl(scopeModelId: string, scopePk?: number): string {
		let params = new HttpParams()
			.set('scopeModelId', scopeModelId);
		if(scopePk) {
			params = params.set('scopePk', scopePk.toString());
		}
		return `${this.serviceUrl}/transfers?${params}`;
	}

	getEventsUrl(scopeModelId: string, scopePk?: number): string {
		let params = new HttpParams()
			.set('scopeModelId', scopeModelId);
		if(scopePk) {
			params = params.set('scopePk', scopePk.toString());
		}
		return `${this.serviceUrl}/events?${params}`;
	}
}
