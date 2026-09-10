import {HttpClient} from '@angular/common/http';
import {Service, inject} from '@angular/core';
import {FieldModelCriterion} from '@core/model/field-model-criterion';
import {Observable} from 'rxjs';
import {APIService} from './api.service';

@Service()
export class ScopeEnrollmentService {
	private serviceUrl: string;

	private readonly http = inject(HttpClient);
	private readonly apiService = inject(APIService);

	constructor() {
		this.serviceUrl = `${this.apiService.getApiUrl()}/scopes`;
	}

	enroll(scopePk: number): Observable<void> {
		return this.http.post<void>(`${this.serviceUrl}/${scopePk}/enrollment/enroll`, {});
	}

	unenroll(scopePk: number): Observable<void> {
		return this.http.post<void>(`${this.serviceUrl}/${scopePk}/enrollment/unenroll`, {});
	}

	countEnrollable(scopePk: number, criteria: FieldModelCriterion[]): Observable<number> {
		return this.http.post<number>(`${this.serviceUrl}/${scopePk}/enrollment/count`, criteria);
	}
}
