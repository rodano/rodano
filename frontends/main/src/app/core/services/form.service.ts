import {HttpClient} from '@angular/common/http';
import {Service, inject} from '@angular/core';
import {Observable} from 'rxjs';
import {Form} from '../model/form';
import {Layout} from '../model/layout';
import {APIService} from './api.service';
import {DatasetSubmission} from '../model/dataset-submission';
import {Dataset} from '../model/dataset';

@Service()
export class FormService {
	private readonly http = inject(HttpClient);
	private readonly apiService = inject(APIService);

	searchOnScope(scopePk: number): Observable<Form[]> {
		return this.http.get<Form[]>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/forms`);
	}

	searchOnEvent(scopePk: number, eventPk: number): Observable<Form[]> {
		return this.http.get<Form[]>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/forms`);
	}

	get(scopePk: number, eventPk: number | undefined, formPk: number) {
		if(eventPk) {
			return this.getForEvent(scopePk, eventPk, formPk);
		}
		return this.getForScope(scopePk, formPk);
	}

	getForScope(scopePk: number, formPk: number): Observable<Form> {
		return this.http.get<Form>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/forms/${formPk}`);
	}

	getForEvent(scopePk: number, eventPk: number, formPk: number): Observable<Form> {
		return this.http.get<Form>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/forms/${formPk}`);
	}

	submit(scopePk: number, eventPk: number | undefined, formPk: number, data: DatasetSubmission): Observable<Dataset[]> {
		let url = `${this.apiService.getApiUrl()}/scopes/${scopePk}`;
		if(eventPk) {
			url = `${url}/events/${eventPk}`;
		}
		return this.http.put<Dataset[]>(`${url}/forms/${formPk}`, data);
	}

	getLayouts(scopePk: number, eventPk: number | undefined, formPk: number): Observable<Layout[]> {
		let url = `${this.apiService.getApiUrl()}/scopes/${scopePk}`;
		if(eventPk) {
			url = `${url}/events/${eventPk}`;
		}
		return this.http.get<Layout[]>(`${url}/forms/${formPk}/layouts`);
	}
}
