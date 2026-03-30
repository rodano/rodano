import {Observable} from 'rxjs';
import {Rule} from '@core/model/rule';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {RuleConstraint} from '@core/model/rule-constraint';

@Injectable({providedIn: 'root'})
export class RuleService {
	constructor(private http: HttpClient) {}

	private base(projectId: string, entityPath: string): string {
		return `/api/superuser/configurator/projects/${projectId}/config/${entityPath}/rules`;
	}

	private constraintBase(projectId: string, entityPath: string): string {
		return `/api/superuser/configurator/projects/${projectId}/config/${entityPath}/constraint`;
	}

	getRules(projectId: string, entityPath: string): Observable<Rule[]> {
		return this.http.get<Rule[]>(this.base(projectId, entityPath));
	}

	getConstraint(projectId: string, entityPath: string): Observable<RuleConstraint> {
		return this.http.get<RuleConstraint>(this.constraintBase(projectId, entityPath));
	}

	saveConstraint(projectId: string, entityPath: string, constraint: RuleConstraint): Observable<RuleConstraint> {
		return this.http.put<RuleConstraint>(this.constraintBase(projectId, entityPath), constraint);
	}

	getAllTags(projectId: string): Observable<string[]> {
		return this.http.get<string[]>(`/api/superuser/configurator/projects/${projectId}/config/rules/tags`);
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
