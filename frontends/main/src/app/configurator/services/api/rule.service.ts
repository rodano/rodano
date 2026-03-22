import {Observable} from 'rxjs';
import {Rule} from '@core/model/rule';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';

@Injectable({providedIn: 'root'})
export class RuleService {
	constructor(private http: HttpClient) {}

	private base(projectId: string, entityPath: string): string {
		return `/api/superuser/configurator/projects/${projectId}/config/${entityPath}/rules`;
	}

	getRules(projectId: string, entityPath: string): Observable<Rule[]> {
		return this.http.get<Rule[]>(this.base(projectId, entityPath));
	}

	createRule(projectId: string, entityPath: string, rule: Rule): Observable<Rule> {
		return this.http.post<Rule>(this.base(projectId, entityPath), rule);
	}

	updateRule(projectId: string, entityPath: string, ruleId: string, rule: Rule): Observable<Rule> {
		return this.http.put<Rule>(`${this.base(projectId, entityPath)}/${ruleId}`, rule);
	}

	deleteRule(projectId: string, entityPath: string, ruleId: string): Observable<void> {
		return this.http.delete<void>(`${this.base(projectId, entityPath)}/${ruleId}`);
	}
}
