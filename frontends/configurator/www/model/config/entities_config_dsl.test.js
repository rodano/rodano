import '../../basic-tools/extension.js';

import {RuleCondition} from './entities/rule_condition.js';

export default async function test(bundle, assert) {
	bundle.begin();

	const result = RuleCondition.parseFullDSL('EVENT.WORKFLOW[ID=DATA_ENTRY][STATUS!=(REVIEWING|REVIEWED)]');

	await bundle.describe('RuleCondition#parseFullDSL', async feature => {
		await feature.it('parses a string properly', () => {
			assert.equal(result.entity.name, 'EVENT', 'Rule condition entity name is "EVENT"');

			//root condition
			let condition = result.condition;
			assert.equal(condition.criterion.property, 'WORKFLOW', 'Root condition property is "WORKFLOW"');
			assert.undefined(condition.criterion.operator, 'Root condition does not have any operator');
			assert.ok(condition.criterion.values.isEmpty(), 'Root condition does not have any value');

			//child condition
			condition = condition.conditions[0];
			assert.equal(condition.criterion.property, 'ID', 'Child condition property is "ID"');
			assert.equal(condition.criterion.operator, 'EQUALS', 'Child condition operator is "EQUALS"');
			assert.equal(condition.criterion.values.length, 1, 'Child condition has 1 value');
			assert.equal(condition.criterion.values[0], 'DATA_ENTRY', 'Child condition first value is "DATA_ENTRY"');

			//grand child condition
			condition = condition.conditions[0];
			assert.equal(condition.criterion.property, 'STATUS', 'Grand child condition property is "ID"');
			assert.equal(condition.criterion.operator, 'NOT_EQUALS', 'Grand child condition operator is "NOT_EQUALS"');
			assert.equal(condition.criterion.values.length, 2, 'Grand child condition has 2 values');
			assert.equal(condition.criterion.values[0], 'REVIEWING', 'Grand child condition first value is "REVIEWING"');
			assert.equal(condition.criterion.values[1], 'REVIEWED', 'Grand child condition second value is "REVIEWED"');
		});
	});

	bundle.end();
}
