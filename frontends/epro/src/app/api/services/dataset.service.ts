import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { APIService } from './api.service';
import { Observable } from 'rxjs';
import { Dataset } from '../model/dataset-dto';
import { HttpParamsService } from './http-helper.service';
import { DatasetCreation } from '../model/dataset-creation-dto';
import { DatasetUpdate } from '../model/dataset-update-dto';

@Injectable({
	providedIn: 'root'
})
export class DatasetService {

	constructor(
		private http: HttpClient,
		private apiService: APIService,
		private httpHelperService: HttpParamsService
	) { }

	public getDatasetsForScope(
		scopePk: number,
		datasetModelIds?: string[]
	): Observable<Dataset[]> {
		if(datasetModelIds) {
			const queryParams = this.httpHelperService.toHttpParams(datasetModelIds);
			return this.http.get<Dataset[]>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/datasets`, { params: queryParams });
		}
		return this.http.get<Dataset[]>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/datasets`);
	}

	public getDatasetsForEvent(
		scopePk: number,
		eventPk: number,
		datasetModelIds?: string[]
	): Observable<Dataset[]> {
		if(datasetModelIds) {
			const queryParams = this.httpHelperService.toHttpParams(datasetModelIds);
			return this.http.get<Dataset[]>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/datasets`, { params: queryParams });
		}
		return this.http.get<Dataset[]>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/datasets`);
	}

	public create(
		dataset: DatasetCreation,
		scopePk: number,
		eventPk?: number
	): Observable<Dataset> {
		let url = `${this.apiService.getApiUrl()}/scopes/${scopePk}`;
		if(eventPk) {
			url = `${url}/events/${eventPk}`;
		}
		return this.http.post<Dataset>(`${url}/datasets`, dataset);
	}

	public save(
		dataset: DatasetUpdate,
		scopePk: number,
		eventPk?: number
	): Observable<Dataset> {
		let url = `${this.apiService.getApiUrl()}/scopes/${scopePk}`;
		if(eventPk) {
			url = `${url}/events/${eventPk}`;
		}
		return this.http.put<Dataset>(`${url}/datasets/${dataset.pk}`, dataset);
	}

}
