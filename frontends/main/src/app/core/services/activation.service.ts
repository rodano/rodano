import {HttpClient} from '@angular/common/http';
import {Service, inject} from '@angular/core';
import {Observable} from 'rxjs';
import {APIService} from './api.service';
import {User} from '../model/user';
import {UserPrivacyPolicies} from '../model/user-privacy-policies';

@Service()
export class ActivationService {
	private readonly http = inject(HttpClient);
	private readonly apiService = inject(APIService);

	getPrivacyPolicies(activationCode: string): Observable<UserPrivacyPolicies> {
		return this.http.get<UserPrivacyPolicies>(`${this.apiService.getApiUrl()}/user/activation/${activationCode}`);
	}

	activateRole(activationCode: string, password: string) {
		return this.http.post<User>(
			`${this.apiService.getApiUrl()}/user/activation/${activationCode}`,
			{
				acceptPolicies: true,
				password: password
			}
		);
	}
}
