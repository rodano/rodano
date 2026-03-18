import {RuleDefinitionActionManagerService} from '../manager/rule-definition-action-manager.service';
import {RuleDefinitionAction} from '@core/model/rule-definition-action';

export interface RuleDefinitionActionContext {
	ruleDefinitionActionManager: RuleDefinitionActionManagerService;
	ruleDefinitionActions: RuleDefinitionAction[];
	originalRuleDefinitionActions: RuleDefinitionAction[];
	modifiedRuleDefinitionActionIds: Set<string>;
}
