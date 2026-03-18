import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {BaseManagerService} from './base-manager.service';
import {RuleDefinitionProperty} from '@core/model/rule-definition-property';
import {RuleDefinitionPropertyService} from '../api/rule-definition-property.service';

@Injectable({providedIn: 'root'})
export class RuleDefinitionPropertyManagerService extends BaseManagerService<RuleDefinitionProperty> {
	constructor(private ruleDefinitionPropertyService: RuleDefinitionPropertyService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {return (rdp: RuleDefinitionProperty) => rdp.ruleDefinitionPropertyId;}
	protected getSimpleFields(): (keyof RuleDefinitionProperty)[] {
		return ['id', 'label', 'entity', 'target', 'type', 'configurationEntity', 'options'];
	}

	protected getTranslationFields(): (keyof RuleDefinitionProperty)[] {
		return [];
	}

	protected getArrayFields(): (keyof RuleDefinitionProperty)[] {
		return [];
	}

	protected fetchAll(projectId: string): Observable<RuleDefinitionProperty[]> {
		return this.ruleDefinitionPropertyService.getRuleDefinitionProperties(projectId);
	}

	protected createEntity(projectId: string, entity: RuleDefinitionProperty): Observable<RuleDefinitionProperty> {
		return this.ruleDefinitionPropertyService.createRuleDefinitionProperty(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.ruleDefinitionPropertyService.deleteRuleDefinitionProperty(projectId, id);
	}
}
