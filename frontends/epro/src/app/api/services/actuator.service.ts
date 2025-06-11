import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { APIService } from './api.service';
import { Observable } from 'rxjs';

@Injectable()
export class ActuatorService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {}

	getParameter(parameter: string): Observable<any> {
		return this.http.get<any>(`${this.apiService.getApiUrl()}/actuator/${parameter}`);
	}

}
