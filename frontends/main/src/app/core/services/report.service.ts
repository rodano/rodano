import {HttpParams} from '@angular/common/http';
import {Service, inject} from '@angular/core';
import {APIService} from './api.service';

@Service()
export class ReportService {
	private serviceUrl: string;

	private readonly apiService = inject(APIService);

	constructor() {
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
