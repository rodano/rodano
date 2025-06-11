import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {HttpParamsService} from './http-params.service';
import {APIService} from './api.service';
import {Observable} from 'rxjs';
import {MailDTO} from '../model/mail-dto';
import {MailSearch} from '../utilities/search/mail-search';
import {PagedResultMailDTO} from '../model/paged-result-mail-dto';
import {reviveDates} from '../decorators/revive-dates.decorator';
import {MailCreationDTO} from '../model/mail-creation-dto';

@Injectable({
	providedIn: 'root'
})
export class MailsService {
	private serviceUrl: string;

	constructor(
		private http: HttpClient,
		private httpParamsService: HttpParamsService,
		private apiService: APIService
	) {
		this.serviceUrl = `${this.apiService.getApiUrl()}/mails`;
	}

	@reviveDates
	get(mailPk: number): Observable<MailDTO> {
		return this.http.get<MailDTO>(`${this.serviceUrl}/${mailPk}`);
	}

	@reviveDates
	search(search: MailSearch): Observable<PagedResultMailDTO> {
		const params = this.httpParamsService.toHttpParams(search);
		return this.http.get<PagedResultMailDTO>(this.serviceUrl, {params});
	}

	getExportUrl(search: MailSearch): string {
		const params = this.httpParamsService.toHttpParams(search, ['pageSize', 'pageIndex', 'sortBy', 'orderAscending']);
		return `${this.serviceUrl}/export?${params}`;
	}

	send(mail: MailCreationDTO): Observable<MailDTO> {
		return this.http.post<MailDTO>(this.serviceUrl, mail);
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

	resendMails(mails: MailDTO[]): Observable<string> {
		let params = new HttpParams();
		mails.map(mail => mail.pk).forEach(mailPk => {
			params = params.append('mailPks', mailPk);
		});

		return this.http.post<string>(`${this.serviceUrl}/resend`, null, {params});
	}
}
