import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
	name: 'lookupById'
})
export class LookupByIdPipe<T extends {id: string}> implements PipeTransform {
	transform(id: string, objects: T[]): T | undefined {
		return objects?.find(o => o.id === id);
	}
}
