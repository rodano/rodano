import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
	name: 'sort'
})
export class ArraySortPipe<T extends Record<string, any>> implements PipeTransform {
	transform(objects: [], field: string): T[] {
		if(!objects) {
			return [];
		}
		return [...objects].sort((a: T, b: T) => {
			if(a[field] < b[field]) {
				return -1;
			}
			else if(a[field] > b[field]) {
				return 1;
			}
			else {
				return 0;
			}
		});
	}
}
