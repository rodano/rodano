import {HttpClient} from '@angular/common/http';
import {Service, inject} from '@angular/core';
import {Observable} from 'rxjs';
import {APIService} from './api.service';
import {Session} from '../model/session';
import {reviveDates} from '../decorators/revive-dates.decorator';

@Service()
export class SessionService {
	private readonly http = inject(HttpClient);
	private readonly apiService = inject(APIService);

	@reviveDates
	get(): Observable<Session[]> {
		return this.http.get<Session[]>(`${this.apiService.getApiUrl()}/sessions`);
	}

	delete(sessionPk: number): Observable<void> {
		return this.http.delete<void>(`${this.apiService.getApiUrl()}/sessions/${sessionPk}`);
	}
}
