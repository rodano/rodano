import { Injectable } from '@angular/core';
import { Field } from '../model/field-dto';

@Injectable({
	providedIn: 'root'
})
export class FieldService {

	isBlank(field: Field): boolean {
		return field.value === undefined || field.value === null || field.value === '';
	}
}
