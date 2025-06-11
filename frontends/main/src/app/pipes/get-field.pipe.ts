import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
	name: 'getField'
})
export class GetFieldPipe implements PipeTransform {
	transform(object: any, key: string): any {
		return object?.[key];
	}
}
