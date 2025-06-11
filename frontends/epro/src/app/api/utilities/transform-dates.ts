import { isValid, parseISO } from 'date-fns';
import { map } from 'rxjs/operators';

function isDate(obj: any) {
	return (typeof obj === 'string' || obj instanceof String) && isValid(new Date((obj as string)));
}

function convertDateProperties(obj: any) {
	Object.keys(obj)
		.filter(key => obj[key] && isDate(obj[key]))
		.forEach(key => {
			obj[key] = parseISO(obj[key]);
		});
	return obj;
}

export function transformDates(target: object, key: string, descriptor: PropertyDescriptor): any {
	const originalMethod = descriptor.value;
	descriptor.value = function(...args: any[]) {
		return originalMethod.apply(this, args).pipe(
			map(obj => {
				if(Array.isArray(obj)) {
					return obj.map((element) => {
						return Object.assign({}, element, convertDateProperties(element));
					});
				} else {
					return Object.assign({}, obj, convertDateProperties(obj));
				}
			})
		);
	};
}
