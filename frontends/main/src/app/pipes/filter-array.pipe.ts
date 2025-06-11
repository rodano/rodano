import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
	name: 'filter'
})
export class FilterArrayPipe<T extends Record<string, any>> implements PipeTransform {
	transform(objects: T[], key: string, value: string): T[] {
		if(!objects) {
			return [];
		}
		return objects.filter(o => (o[key] as unknown as any).toString() === value);
	}
}
