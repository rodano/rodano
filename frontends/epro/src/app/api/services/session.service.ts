import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Session } from '../model/session-dto';
import { APIService } from './api.service';

@Injectable()
export class SessionService {

	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {
	}

	get(): Observable<Session[]> {
		return this.http.get<Session[]>(`${this.apiService.getApiUrl()}/sessions`);
	}

	delete(sessionPk: number) {
		return this.http.delete(`${this.apiService.getApiUrl()}/sessions/${sessionPk}`);
	}

}
