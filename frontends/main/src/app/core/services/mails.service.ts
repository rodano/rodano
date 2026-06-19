import {Service, inject} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {HttpParamsService} from './http-params.service';
import {APIService} from './api.service';
import {Observable} from 'rxjs';
import {Mail} from '../model/mail';
import {MailSearch} from '../utilities/search/mail-search';
import {PagedResultMail} from '../model/paged-result-mail';
import {reviveDates} from '../decorators/revive-dates.decorator';
import {MailCreation} from '../model/mail-creation';

@Service()
export class MailsService {
	private serviceUrl: string;

	private readonly http = inject(HttpClient);
	private readonly httpParamsService = inject(HttpParamsService);
	private readonly apiService = inject(APIService);

	constructor() {
		this.serviceUrl = `${this.apiService.getApiUrl()}/mails`;
	}

	@reviveDates
	get(mailPk: number): Observable<Mail> {
		return this.http.get<Mail>(`${this.serviceUrl}/${mailPk}`);
	}

	@reviveDates
	search(search: MailSearch): Observable<PagedResultMail> {
		const params = this.httpParamsService.toHttpParams(search);
		return this.http.get<PagedResultMail>(this.serviceUrl, {params});
	}

	getExportUrl(search: MailSearch): string {
		const params = this.httpParamsService.toHttpParams(search, ['pageSize', 'pageIndex', 'sortBy', 'orderAscending']);
		return `${this.serviceUrl}/export?${params}`;
	}

	send(mail: MailCreation): Observable<Mail> {
		return this.http.post<Mail>(this.serviceUrl, mail);
	}

	getAttachmentByPk(mailPk: number, attPk: number): string {
		return `${this.serviceUrl}/${mailPk}/attachments/${attPk}`;
	}

	getOrigins(): Observable<string[]> {
		return this.http.get<string[]>(`${this.serviceUrl}/origins`);
	}

	getStatuses(): Observable<string[]> {
		return this.http.get<string[]>(`${this.serviceUrl}/statuses`);
	}

	resendMails(mails: Mail[]): Observable<string> {
		let params = new HttpParams();
		mails.map(mail => mail.pk).forEach(mailPk => {
			params = params.append('mailPks', mailPk);
		});

		return this.http.post<string>(`${this.serviceUrl}/resend`, null, {params});
	}
}
