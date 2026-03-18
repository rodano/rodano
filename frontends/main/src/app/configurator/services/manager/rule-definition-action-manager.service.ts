import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {BaseManagerService} from './base-manager.service';
import {RuleDefinitionAction} from '@core/model/rule-definition-action';
import {RuleDefinitionActionService} from '../api/rule-definition-action.service';

@Injectable({providedIn: 'root'})
export class RuleDefinitionActionManagerService extends BaseManagerService<RuleDefinitionAction> {
	constructor(private ruleDefinitionActionService: RuleDefinitionActionService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {return (rda: RuleDefinitionAction) => rda.ruleDefinitionActionId;}
	protected getSimpleFields(): (keyof RuleDefinitionAction)[] {
		return ['id', 'label', 'entity'];
	}

	protected getTranslationFields(): (keyof RuleDefinitionAction)[] {
		return [];
	}

	protected getArrayFields(): (keyof RuleDefinitionAction)[] {
		return ['parameters'];
	}

	protected fetchAll(projectId: string): Observable<RuleDefinitionAction[]> {
		return this.ruleDefinitionActionService.getRuleDefinitionActions(projectId);
	}

	protected createEntity(projectId: string, entity: RuleDefinitionAction): Observable<RuleDefinitionAction> {
		return this.ruleDefinitionActionService.createRuleDefinitionAction(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.ruleDefinitionActionService.deleteRuleDefinitionAction(projectId, id);
	}
}
