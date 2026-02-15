import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Validator} from '@core/model/validator';

@Injectable({
	providedIn: 'root'
})
export class ValidatorService {
	constructor(private http: HttpClient) {}

	getValidators(projectId: string): Observable<Validator[]> {
		return this.http.get<Validator[]>(`/api/superuser/configurator/projects/${projectId}/config/validators`);
	}

	getValidator(projectId: string, validatorId: string): Observable<Validator> {
		return this.http.get<Validator>(`/api/superuser/configurator/projects/${projectId}/config/validators/${validatorId}`);
	}

	createValidator(projectId: string, validator: Validator): Observable<Validator> {
		return this.http.post<Validator>(`/api/superuser/configurator/projects/${projectId}/config/validators`, validator);
	}

	updateValidator(projectId: string, validatorId: string, validator: Validator): Observable<Validator> {
		return this.http.put<Validator>(`/api/superuser/configurator/projects/${projectId}/config/validators/${validatorId}`, validator);
	}

	deleteValidator(projectId: string, validatorId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/validators/${validatorId}`);
	}
}
