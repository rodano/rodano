import {tap} from 'rxjs/operators';
import {reviveObjectDates} from './revive-dates-helper';

export function reviveDates(target: object, key: string, descriptor: PropertyDescriptor): any {
	const originalMethod = descriptor.value;
	descriptor.value = function (...args: any[]) {
		return originalMethod.apply(this, args).pipe(tap(reviveObjectDates));
	};
}
