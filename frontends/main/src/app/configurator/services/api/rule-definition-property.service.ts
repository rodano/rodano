import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {RuleDefinitionProperty} from '@core/model/rule-definition-property';

@Injectable({
	providedIn: 'root'
})
export class RuleDefinitionPropertyService {
	constructor(private http: HttpClient) {}

	getRuleDefinitionProperties(projectId: string): Observable<RuleDefinitionProperty[]> {
		return this.http.get<RuleDefinitionProperty[]>(`/api/superuser/configurator/projects/${projectId}/config/rule-def-properties`);
	}

	getRuleDefinitionProperty(projectId: string, ruleDefinitionPropertyId: string): Observable<RuleDefinitionProperty> {
		return this.http.get<RuleDefinitionProperty>(`/api/superuser/configurator/projects/${projectId}/config/rule-def-properties/${ruleDefinitionPropertyId}`);
	}

	createRuleDefinitionProperty(projectId: string, ruleDefinitionProperty: RuleDefinitionProperty): Observable<RuleDefinitionProperty> {
		return this.http.post<RuleDefinitionProperty>(`/api/superuser/configurator/projects/${projectId}/config/rule-def-properties`, ruleDefinitionProperty);
	}

	updateRuleDefinitionProperty(projectId: string, ruleDefinitionPropertyId: string, ruleDefinitionProperty: RuleDefinitionProperty): Observable<RuleDefinitionProperty> {
		return this.http.put<RuleDefinitionProperty>(`/api/superuser/configurator/projects/${projectId}/config/rule-def-properties/${ruleDefinitionPropertyId}`, ruleDefinitionProperty);
	}

	deleteRuleDefinitionProperty(projectId: string, ruleDefinitionPropertyId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/rule-def-properties/${ruleDefinitionPropertyId}`);
	}
}
