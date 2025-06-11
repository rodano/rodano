import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {APIService} from './api.service';
import {Observable} from 'rxjs';
import {DatasetDTO} from '../model/dataset-dto';
import {HttpParamsService} from './http-params.service';

@Injectable({
	providedIn: 'root'
})
export class DatasetService {
	private serviceUrl: string;

	constructor(
		private http: HttpClient,
		private apiService: APIService,
		private httpHelperService: HttpParamsService
	) {
		this.serviceUrl = `${this.apiService.getApiUrl()}/scopes`;
	}

	searchOnScope(scopePk: number, datasetModelIds?: string[]): Observable<DatasetDTO[]> {
		if(datasetModelIds) {
			const params = this.httpHelperService.toHttpParams(datasetModelIds);
			return this.http.get<DatasetDTO[]>(`${this.serviceUrl}/${scopePk}/datasets`, {params});
		}
		return this.http.get<DatasetDTO[]>(`${this.serviceUrl}/${scopePk}/datasets`);
	}

	searchOnEvent(scopePk: number, eventPk: number, datasetModelIds?: string[]): Observable<DatasetDTO[]> {
		if(datasetModelIds) {
			const params = this.httpHelperService.toHttpParams(datasetModelIds);
			return this.http.get<DatasetDTO[]>(`${this.serviceUrl}/${scopePk}/events/${eventPk}/datasets`, {params});
		}
		return this.http.get<DatasetDTO[]>(`${this.serviceUrl}/${scopePk}/events/${eventPk}/datasets`);
	}

	searchOnForm(scopePk: number, eventPk: number | undefined, formPk: number): Observable<DatasetDTO[]> {
		let url = `${this.serviceUrl}/${scopePk}`;
		if(eventPk) {
			url = `${url}/events/${eventPk}`;
		}
		return this.http.get<DatasetDTO[]>(`${url}/forms/${formPk}/datasets`);
	}

	getCandidate(scopePk: number, eventPk: number | undefined, datasetModelId: string): Observable<DatasetDTO> {
		let url = `${this.serviceUrl}/${scopePk}`;
		if(eventPk) {
			url = `${url}/events/${eventPk}`;
		}
		const params = new HttpParams()
			.set('datasetModelId', datasetModelId);
		return this.http.get<DatasetDTO>(`${url}/datasets/candidate`, {params});
	}

	saveForScope(scopePk: number, datasets: DatasetDTO[]): Observable<DatasetDTO[]> {
		const url = `${this.serviceUrl}/${scopePk}/datasets`;
		return this.http.post<DatasetDTO[]>(url, datasets);
	}

	saveForEvent(scopePk: number, eventPk: number, datasets: DatasetDTO[]): Observable<DatasetDTO[]> {
		const url = `${this.serviceUrl}/${scopePk}/events/${eventPk}/datasets`;
		return this.http.post<DatasetDTO[]>(url, datasets);
	}
}
