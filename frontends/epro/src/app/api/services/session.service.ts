import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SessionDTO } from '../model/session-dto';
import { APIService } from './api.service';

@Injectable()
export class SessionService {

	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {
	}

	get(): Observable<SessionDTO[]> {
		return this.http.get<SessionDTO[]>(`${this.apiService.getApiUrl()}/sessions`);
	}

	delete(sessionPk: number) {
		return this.http.delete(`${this.apiService.getApiUrl()}/sessions/${sessionPk}`);
	}

}
