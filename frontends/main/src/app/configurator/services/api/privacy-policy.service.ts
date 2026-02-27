import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PrivacyPolicy} from '@core/model/privacy-policy';

@Injectable({
	providedIn: 'root'
})
export class PrivacyPolicyService {
	constructor(private http: HttpClient) {}

	getPrivacyPolicies(projectId: string): Observable<PrivacyPolicy[]> {
		return this.http.get<PrivacyPolicy[]>(`/api/superuser/configurator/projects/${projectId}/config/policies`);
	}

	getPrivacyPolicy(projectId: string, privacyPolicyId: string): Observable<PrivacyPolicy> {
		return this.http.get<PrivacyPolicy>(`/api/superuser/configurator/projects/${projectId}/config/policies/${privacyPolicyId}`);
	}

	createPrivacyPolicy(projectId: string, privacyPolicy: PrivacyPolicy): Observable<PrivacyPolicy> {
		return this.http.post<PrivacyPolicy>(`/api/superuser/configurator/projects/${projectId}/config/policies`, privacyPolicy);
	}

	updatePrivacyPolicy(projectId: string, privacyPolicyId: string, privacyPolicy: PrivacyPolicy): Observable<PrivacyPolicy> {
		return this.http.put<PrivacyPolicy>(`/api/superuser/configurator/projects/${projectId}/config/policies/${privacyPolicyId}`, privacyPolicy);
	}

	deletePrivacyPolicy(projectId: string, privacyPolicyId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/policies/${privacyPolicyId}`);
	}
}
