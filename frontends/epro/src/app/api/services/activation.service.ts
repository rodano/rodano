import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Registration } from '../model/registration';
import { APIService } from './api.service';

@Injectable({
	providedIn: 'root'
})
export class ActivationService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) { }

	getActivationPolicies(pinCode: string): Observable<Registration> {
		return this.http.get<Registration>(`${this.apiService.getApiUrl()}/activation/${pinCode}/policies`);
	}

	activateRole(pinCode: string, password: string) {
		return this.http.post(`${this.apiService.getApiUrl()}/activation/${pinCode}`, { agreePolicy: true, password });
	}
}
