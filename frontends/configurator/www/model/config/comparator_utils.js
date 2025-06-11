import '../../basic-tools/extension.js';

export class ComparatorUtils {
	static compareField(object_1, object_2, field) {
		const value_1 = Function.isFunction(field) ? field.call(object_1) : object_1[field];
		const value_2 = Function.isFunction(field) ? field.call(object_2) : object_2[field];
		if(value_1 !== undefined && value_2 === undefined) {
			return -1;
		}
		if(value_1 === undefined && value_2 !== undefined) {
			return 1;
		}
		//handles case when both values are undefined
		if(value_1 === value_2) {
			return 0;
		}
		return value_1.compareTo(value_2);
	}

	static compareFields(object_1, object_2, fields) {
		let index = 0;
		let comparison = 0;
		do {
			comparison = ComparatorUtils.compareField(object_1, object_2, fields[index]);
			index++;
		}
		while(index < fields.length && comparison === 0);
		return comparison;
	}
}
