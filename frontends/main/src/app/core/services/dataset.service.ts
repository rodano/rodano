import {Service, inject} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {APIService} from './api.service';
import {Observable} from 'rxjs';
import {Dataset} from '../model/dataset';
import {HttpParamsService} from './http-params.service';

@Service()
export class DatasetService {
	private serviceUrl: string;

	private readonly http = inject(HttpClient);
	private readonly apiService = inject(APIService);
	private readonly httpHelperService = inject(HttpParamsService);

	constructor() {
		this.serviceUrl = `${this.apiService.getApiUrl()}/scopes`;
	}

	searchOnScope(scopePk: number, datasetModelIds?: string[]): Observable<Dataset[]> {
		if(datasetModelIds) {
			const params = this.httpHelperService.toHttpParams(datasetModelIds);
			return this.http.get<Dataset[]>(`${this.serviceUrl}/${scopePk}/datasets`, {params});
		}
		return this.http.get<Dataset[]>(`${this.serviceUrl}/${scopePk}/datasets`);
	}

	searchOnEvent(scopePk: number, eventPk: number, datasetModelIds?: string[]): Observable<Dataset[]> {
		if(datasetModelIds) {
			const params = this.httpHelperService.toHttpParams(datasetModelIds);
			return this.http.get<Dataset[]>(`${this.serviceUrl}/${scopePk}/events/${eventPk}/datasets`, {params});
		}
		return this.http.get<Dataset[]>(`${this.serviceUrl}/${scopePk}/events/${eventPk}/datasets`);
	}

	searchOnForm(scopePk: number, eventPk: number | undefined, formPk: number): Observable<Dataset[]> {
		let url = `${this.serviceUrl}/${scopePk}`;
		if(eventPk) {
			url = `${url}/events/${eventPk}`;
		}
		return this.http.get<Dataset[]>(`${url}/forms/${formPk}/datasets`);
	}

	getCandidate(scopePk: number, eventPk: number | undefined, datasetModelId: string): Observable<Dataset> {
		let url = `${this.serviceUrl}/${scopePk}`;
		if(eventPk) {
			url = `${url}/events/${eventPk}`;
		}
		const params = new HttpParams()
			.set('datasetModelId', datasetModelId);
		return this.http.get<Dataset>(`${url}/candidate-dataset`, {params});
	}

	saveForScope(scopePk: number, datasets: Dataset[]): Observable<Dataset[]> {
		const url = `${this.serviceUrl}/${scopePk}/datasets`;
		return this.http.post<Dataset[]>(url, datasets);
	}

	saveForEvent(scopePk: number, eventPk: number, datasets: Dataset[]): Observable<Dataset[]> {
		const url = `${this.serviceUrl}/${scopePk}/events/${eventPk}/datasets`;
		return this.http.post<Dataset[]>(url, datasets);
	}
}
