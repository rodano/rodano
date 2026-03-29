import {RuleCondition} from '@core/model/rule-condition';
import {RuleCriterion} from '@core/model/rule-criterion';

export interface RuleConditionExtended extends RuleCondition {
	criterion?: RuleCriterion;
	conditions?: RuleConditionExtended[];
}
