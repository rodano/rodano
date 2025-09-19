import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {APIService} from './api.service';
import {ScopeMini} from '../model/scope-mini';
import {DatasetModel} from '../model/dataset-model';

@Injectable({
	providedIn: 'root'
})
export class ExtractService {
	private serviceUrl: string;

	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {
		this.serviceUrl = `${this.apiService.getApiUrl()}/extracts`;
	}

	getRootScopes(): Observable<Record<string, ScopeMini[]>> {
		return this.http.get<Record<string, ScopeMini[]>>(`${this.serviceUrl}/root-scopes`);
	}

	getDatasetModels(scopeModelId: string): Observable<DatasetModel[]> {
		const params = new HttpParams().set('scopeModelId', scopeModelId);
		return this.http.get<DatasetModel[]>(`${this.serviceUrl}/dataset-models`, {params});
	}

	getSpecificationsUrl(datasetModelIds: string[], withModificationDates?: boolean): string {
		let params = new HttpParams()
			.set('datasetModelIds', datasetModelIds.toString());
		if(withModificationDates) {
			params = params.set('withModificationDates', withModificationDates);
		}
		return `${this.serviceUrl}/specifications?${params}`;
	}

	getExportUrl(datasetModelIds: string[], scopePk?: number, withModificationDates?: boolean) {
		let params = new HttpParams()
			.set('datasetModelIds', datasetModelIds.toString());
		if(scopePk) {
			params = params.set('scopePk', scopePk);
		}
		if(withModificationDates) {
			params = params.set('withModificationDates', withModificationDates);
		}
		return `${this.serviceUrl}?${params}`;
	}
}
