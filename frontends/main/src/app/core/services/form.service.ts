import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {FormDTO} from '../model/form-dto';
import {LayoutDTO} from '../model/layout-dto';
import {APIService} from './api.service';
import {DatasetSubmissionDTO} from '../model/dataset-submission-dto';
import {DatasetDTO} from '../model/dataset-dto';

@Injectable({
	providedIn: 'root'
})
export class FormService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) { }

	searchOnScope(scopePk: number): Observable<FormDTO[]> {
		return this.http.get<FormDTO[]>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/forms`);
	}

	searchOnEvent(scopePk: number, eventPk: number): Observable<FormDTO[]> {
		return this.http.get<FormDTO[]>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/forms`);
	}

	get(scopePk: number, eventPk: number | undefined, formPk: number) {
		if(eventPk) {
			return this.getForEvent(scopePk, eventPk, formPk);
		}
		return this.getForScope(scopePk, formPk);
	}

	getForScope(scopePk: number, formPk: number): Observable<FormDTO> {
		return this.http.get<FormDTO>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/forms/${formPk}`);
	}

	getForEvent(scopePk: number, eventPk: number, formPk: number): Observable<FormDTO> {
		return this.http.get<FormDTO>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/forms/${formPk}`);
	}

	submit(scopePk: number, eventPk: number | undefined, formPk: number, data: DatasetSubmissionDTO): Observable<DatasetDTO[]> {
		let url = `${this.apiService.getApiUrl()}/scopes/${scopePk}`;
		if(eventPk) {
			url = `${url}/events/${eventPk}`;
		}
		return this.http.put<DatasetDTO[]>(`${url}/forms/${formPk}`, data);
	}

	getLayouts(scopePk: number, eventPk: number | undefined, formPk: number): Observable<LayoutDTO[]> {
		let url = `${this.apiService.getApiUrl()}/scopes/${scopePk}`;
		if(eventPk) {
			url = `${url}/events/${eventPk}`;
		}
		return this.http.get<LayoutDTO[]>(`${url}/forms/${formPk}/layouts`);
	}
}
