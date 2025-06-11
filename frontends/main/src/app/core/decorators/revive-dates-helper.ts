const dateRegexp = /\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d(\.\d+)?([+-][0-2]\d:[0-5]\d|Z)/;

function isDate(object: any) {
	return typeof object === 'string' && dateRegexp.test(object as string);
}

export function reviveObjectDates(object: any) {
	if(Array.isArray(object)) {
		object.forEach(e => reviveObjectDates(e));
		return;
	}
	for(const [key, value] of Object.entries(object)) {
		if(value) {
			if(typeof value === 'object') {
				reviveObjectDates(value);
			}
			else if(isDate(value)) {
				object[key] = new Date(value as string);
			}
		}
	}
}
