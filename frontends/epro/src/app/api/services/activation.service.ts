import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { UserPrivacyPolicies } from '../model/user-privacy-policies-dto';
import { APIService } from './api.service';

@Injectable({
	providedIn: 'root'
})
export class ActivationService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) { }

	getActivationPolicies(pinCode: string): Observable<UserPrivacyPolicies> {
		return this.http.get<UserPrivacyPolicies>(`${this.apiService.getApiUrl()}/activation/${pinCode}/policies`);
	}

	activateRole(pinCode: string, password: string) {
		return this.http.post(`${this.apiService.getApiUrl()}/activation/${pinCode}`, { agreePolicy: true, password });
	}
}
