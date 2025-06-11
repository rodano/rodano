import { Injectable } from '@angular/core';
import { FieldDTO } from '../model/field-dto';

@Injectable({
	providedIn: 'root'
})
export class FieldService {

	isBlank(field: FieldDTO): boolean {
		return field.value === undefined || field.value === null || field.value === '';
	}
}
