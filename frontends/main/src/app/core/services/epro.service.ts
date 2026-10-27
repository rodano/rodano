import {HttpClient} from '@angular/common/http';
import {Service, inject} from '@angular/core';
import {Observable} from 'rxjs';
import {APIService} from './api.service';
import {EPROInvitation} from '../model/epro-invitation';
import {EproRobot} from '../model/epro-robot';
import {PagedResultEproScope} from '../model/paged-result-epro-scope';
import {ScopeSearch} from '../utilities/search/scope-search';
import {HttpParamsService} from './http-params.service';
import {reviveDates} from '../decorators/revive-dates.decorator';

@Service()
export class EproService {
	private serviceUrl: string;

	private readonly http = inject(HttpClient);
	private readonly apiService = inject(APIService);
	private readonly httpParamsService = inject(HttpParamsService);

	constructor() {
		this.serviceUrl = `${this.apiService.getApiUrl()}/epro`;
	}

	@reviveDates
	searchScopes(search: ScopeSearch): Observable<PagedResultEproScope> {
		const params = this.httpParamsService.toHttpParams(search);
		return this.http.get<PagedResultEproScope>(`${this.serviceUrl}/scopes`, {params});
	}

	getRobot(key: string): Observable<EproRobot> {
		return this.http.post<EproRobot>(`${this.serviceUrl}/robot`, {key});
	}

	invite(scopePk: number): Observable<EPROInvitation> {
		return this.http.put<EPROInvitation>(`${this.serviceUrl}/${scopePk}/invite`, undefined);
	}

	revoke(scopePk: number): Observable<void> {
		return this.http.put<void>(`${this.serviceUrl}/${scopePk}/revoke`, undefined);
	}
}
