import {RuleCondition} from './model/config/entities/rule_condition.js';
import {Config} from './model_config.js';

function draw_condition(entity, condition) {
	const condition_li = document.createFullElement('li', {draggable: false, id: condition.id, style: 'position: relative;'});
	condition_li.condition = condition;

	const condition_error = document.createFullElement('div', {style: 'float: right; display: none; z-index: 2; padding: 2px 0.5rem; border-radius: 2px; background-color: #df8d00; color: #090909;'});
	condition_li.appendChild(condition_error);

	const condition_div = document.createFullElement('div', {contenteditable: true, spellcheck: false}, condition.toDSL(entity));
	condition_div.addEventListener('input', function() {
		condition_error.style.display = 'none';
		this.style.color = 'white';
		try {
			console.log(RuleCondition.parseFullDSL(this.textContent));
		}
		catch(error) {
			this.style.color = 'red';
			condition_error.textContent = error.message;
			condition_error.style.display = 'block';
		}
	});
	condition_li.appendChild(condition_div);

	return condition_li;
}

export const RuleDSL = {
	DrawConditions: function(node, entities, conditions) {
		const rule_conditions_dsl = document.getElementById('rule_conditions_dsl');
		rule_conditions_dsl.empty();
		for(const entity_id in conditions) {
			if(conditions.hasOwnProperty(entity_id)) {
				const entity = Config.Enums.RuleEntities[entity_id];
				//draw conditions
				for(let i = 0; i < conditions[entity_id].conditions.length; i++) {
					rule_conditions_dsl.appendChild(draw_condition(entity, conditions[entity_id].conditions[i]));
				}
			}
		}
	}
};
