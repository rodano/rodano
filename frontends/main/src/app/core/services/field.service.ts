import {Injectable} from '@angular/core';
import {FieldDTO} from '../model/field-dto';
import {FieldModelType} from '../model/field-model-type';
import {FieldModelDTO} from '../model/field-model-dto';

@Injectable({
	providedIn: 'root'
})
export class FieldService {
	isBlank(field: FieldDTO): boolean {
		return field.value === undefined || field.value === null || field.value === '';
	}

	isDate(fieldModel: FieldModelDTO): boolean {
		return fieldModel.withYears;
	}

	isTime(fieldModel: FieldModelDTO): boolean {
		return fieldModel.withHours;
	}

	generateDateFormat(fieldModel: FieldModelDTO): string {
		if(![FieldModelType.DATE, FieldModelType.DATE_SELECT].includes(fieldModel.type)) {
			throw new Error(`Generating a date format is not supported for field model ${fieldModel.id} of type ${fieldModel.type}`);
		}
		let format = '';
		if(fieldModel.withDays) {
			format += 'dd';
		}
		if(fieldModel.withMonths) {
			if(format) {
				format += '.';
			}
			format += 'MM';
		}
		if(fieldModel.withYears) {
			if(format) {
				format += '.';
			}
			format += 'yyyy';
		}
		return format;
	}

	generateTimeFormat(fieldModel: FieldModelDTO): string {
		if(![FieldModelType.DATE, FieldModelType.DATE_SELECT].includes(fieldModel.type)) {
			throw new Error(`Generating a time format is not supported for field model ${fieldModel.id} of type ${fieldModel.type}`);
		}
		let format = '';
		if(fieldModel.withHours) {
			format += 'HH';
		}
		if(fieldModel.withMinutes) {
			if(format) {
				format += ':';
			}
			format += 'mm';
		}
		if(fieldModel.withSeconds) {
			if(format) {
				format += ':';
			}
			format += 'ss';
		}
		return format;
	}

	generateFormat(fieldModel: FieldModelDTO): string {
		if(this.isDate(fieldModel) && this.isTime(fieldModel)) {
			const parts = [this.generateDateFormat(fieldModel), this.generateTimeFormat(fieldModel)];
			return parts.join(' ');
		}
		if(this.isDate(fieldModel)) {
			return this.generateDateFormat(fieldModel);
		}
		return this.generateTimeFormat(fieldModel);
	}
}
