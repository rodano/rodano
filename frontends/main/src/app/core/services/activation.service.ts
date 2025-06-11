import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {APIService} from './api.service';
import {UserDTO} from '../model/user-dto';
import {UserPrivacyPoliciesDTO} from '../model/user-privacy-policies-dto';

@Injectable({
	providedIn: 'root'
})
export class ActivationService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) { }

	getPrivacyPolicies(activationCode: string): Observable<UserPrivacyPoliciesDTO> {
		return this.http.get<UserPrivacyPoliciesDTO>(`${this.apiService.getApiUrl()}/user/activation/${activationCode}`);
	}

	activateRole(activationCode: string, password: string) {
		return this.http.post<UserDTO>(
			`${this.apiService.getApiUrl()}/user/activation/${activationCode}`,
			{
				acceptPolicies: true,
				password: password
			}
		);
	}
}
