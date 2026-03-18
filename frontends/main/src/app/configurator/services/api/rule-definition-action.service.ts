import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {RuleDefinitionAction} from '@core/model/rule-definition-action';

@Injectable({
	providedIn: 'root'
})
export class RuleDefinitionActionService {
	constructor(private http: HttpClient) {}

	getRuleDefinitionActions(projectId: string): Observable<RuleDefinitionAction[]> {
		return this.http.get<RuleDefinitionAction[]>(`/api/superuser/configurator/projects/${projectId}/config/rule-def-actions`);
	}

	getRuleDefinitionAction(projectId: string, ruleDefinitionActionId: string): Observable<RuleDefinitionAction> {
		return this.http.get<RuleDefinitionAction>(`/api/superuser/configurator/projects/${projectId}/config/rule-def-actions/${ruleDefinitionActionId}`);
	}

	createRuleDefinitionAction(projectId: string, ruleDefinitionAction: RuleDefinitionAction): Observable<RuleDefinitionAction> {
		return this.http.post<RuleDefinitionAction>(`/api/superuser/configurator/projects/${projectId}/config/rule-def-actions`, ruleDefinitionAction);
	}

	updateRuleDefinitionAction(projectId: string, ruleDefinitionActionId: string, ruleDefinitionAction: RuleDefinitionAction): Observable<RuleDefinitionAction> {
		return this.http.put<RuleDefinitionAction>(`/api/superuser/configurator/projects/${projectId}/config/rule-def-actions/${ruleDefinitionActionId}`, ruleDefinitionAction);
	}

	deleteRuleDefinitionAction(projectId: string, ruleDefinitionActionId: string): Observable<void> {
		return this.http.delete<void>(`/api/superuser/configurator/projects/${projectId}/config/rule-def-actions/${ruleDefinitionActionId}`);
	}
}
