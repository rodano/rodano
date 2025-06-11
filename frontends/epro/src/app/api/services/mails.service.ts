import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { HttpParamsService } from './http-helper.service';
import { APIService } from './api.service';
import { Observable } from 'rxjs';
import { Mail } from '../model/mail';
import { PagedResult } from '../model/paged-result';
import { MailSearch } from '../model/mail-search';

@Injectable({
	providedIn: 'root'
})
export class MailsService {

	constructor(
		private http: HttpClient,
		private httpHelper: HttpParamsService,
		private apiService: APIService
	) { }

	get(mailPk: number): Observable<Mail> {
		return this.http.get<Mail>(`${this.apiService.getApiUrl()}/mails/${mailPk}`);
	}

	search(predicate: MailSearch): Observable<PagedResult<Mail>> {
		return this.http.get<PagedResult<Mail>>(`${this.apiService.getApiUrl()}/mails`, {params: this.httpHelper.toHttpParams(predicate)});
	}

	getExportUrl(predicate: MailSearch): string {
		const parameters = this.httpHelper.toHttpParams(predicate);
		return `${this.apiService.getApiUrl()}/mails/export?${parameters}`;
	}

	getAttachmentByPk(mailPk: number, attPk: number): string {
		return `${this.apiService.getApiUrl()}/mails/${mailPk}/attachments/${attPk}`;
		// return this.http.get<Blob>(`${this.apiService.getApiUrl()}/mails/${mailPk}/attachments/${attPk}`);
	}

	getOrigins(): Observable<string[]> {
		return this.http.get<string[]>(`${this.apiService.getApiUrl()}/mails/origins`);
	}

	getStatuses(): Observable<string[]> {
		return this.http.get<string[]>(`${this.apiService.getApiUrl()}/mails/statuses`);
	}
}
