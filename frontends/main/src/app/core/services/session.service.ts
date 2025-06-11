import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {APIService} from './api.service';
import {SessionDTO} from '../model/session-dto';
import {reviveDates} from '../decorators/revive-dates.decorator';

@Injectable({
	providedIn: 'root'
})
export class SessionService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {
	}

	@reviveDates
	get(): Observable<SessionDTO[]> {
		return this.http.get<SessionDTO[]>(`${this.apiService.getApiUrl()}/sessions`);
	}

	delete(sessionPk: number): Observable<void> {
		return this.http.delete<void>(`${this.apiService.getApiUrl()}/sessions/${sessionPk}`);
	}
}
