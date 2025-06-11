import '../../basic-tools/extension.js';

export class Utils {
	static getObjectById(objects, object_id) {
		const object = objects.find(o => o.id === object_id);
		if(object) {
			return object;
		}
		throw new Error(`No object with id ${object_id}`);
	}

	static getLocalizedField(field, languages) {
		const labels = this[field];
		if(labels && !Object.isEmpty(labels)) {
			if(Array.isArray(languages)) {
				for(let i = 0; i < languages.length; i++) {
					const label = labels[languages[i]];
					if(label) {
						return label;
					}
				}
			}
			else {
				const label = labels[languages];
				if(label) {
					return label;
				}
			}
		}
		return '';
	}

	static checkText(text) {
		if(text) {
			for(let i = 0; i < text.length; i++) {
				const code = text.charCodeAt(i);
				if(code > 256) {
					return `Illegal character ${text.charAt(i)}`;
				}
			}
			if(text.match(/\s{2,}/)) {
				return 'Contains consecutive white spaces';
			}
		}
		return undefined;
	}

	static shortenId(id) {
		//replace BLA_BLA_BLA by by B_B_B
		return id.replace(/([A-Z0-9])[A-Z0-9]+/g, '$1');
	}

	static generateChildConditionId(condition, prefix) {
		let good = false;
		let index = 1;
		let id;
		while(!good) {
			id = `${prefix}${index}`;
			//check if there is already a condition with same id in child conditions
			if(condition.conditions.some(c => c.id === id)) {
				index++;
			}
			else {
				//check if there is already a condition with same id in all rule conditions
				try {
					condition.constraint.getCondition(id);
					index++;
				}
				catch {
					good = true;
				}
			}
		}
		return id;
	}

	static mergeConditions(container, conditions, aggressive) {
		//update parent
		conditions.forEach(c => c.parent = container);
		//prepare list of matching condition ids
		//when the agressive flag is used, the merge tries to remove duplicate conditions
		//the ids of these duplicated conditions will be lost and must be replaced by the id of the condition that remains
		const matching_ids = {};
		if(aggressive) {
			conditions.forEach(condition => {
				//look in this conditions if there is one identical condition
				const same_condition = container.conditions.find(c => c.equals(condition));
				if(same_condition) {
					matching_ids[condition.id] = same_condition.id;
					Object.assign(matching_ids, Utils.mergeConditions(same_condition, condition.conditions, aggressive));
				}
				else {
					container.conditions.push(condition);
				}
			});
		}
		//this is the naive way to merge two conditions lists, duplicates won't be removed
		else {
			container.conditions.pushAll(conditions);
		}
		return matching_ids;
	}
}
