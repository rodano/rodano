import {Action} from './action.js';
import {Rule} from './rule.js';
import {RuleConstraint} from './rule_constraint.js';

export default async function test(bundle, assert) {
	bundle.begin();

	await bundle.describe('RuleConstraint#getContainer', async feature => {
		await feature.it('retrieve the container properly', () => {
			const action = new Action({
				id: 'DO',
				shortname: {
					en: 'Do',
					fr: 'Faire'
				}
			});
			const rule = new Rule();
			rule.rulable = action;
			const constraint = new RuleConstraint();
			constraint.constrainable = rule;
			rule.constraint = constraint;

			assert.equal(constraint.constrainable, rule, 'Rule constraint constrainable gives the rule');
			assert.equal(constraint.getContainer(), action, 'Rule constraint container gives the action');
		});
	});

	bundle.end();
}
