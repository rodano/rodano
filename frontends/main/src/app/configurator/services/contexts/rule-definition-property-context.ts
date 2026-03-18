import {RuleDefinitionPropertyManagerService} from '../manager/rule-definition-property-manager.service';
import {RuleDefinitionProperty} from '@core/model/rule-definition-property';

export interface RuleDefinitionPropertyContext {
	ruleDefinitionPropertyManager: RuleDefinitionPropertyManagerService;
	ruleDefinitionProperties: RuleDefinitionProperty[];
	originalRuleDefinitionProperties: RuleDefinitionProperty[];
	modifiedRuleDefinitionPropertyIds: Set<string>;
}
