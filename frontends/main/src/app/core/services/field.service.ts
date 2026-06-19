import {Service} from '@angular/core';
import {Field} from '../model/field';
import {FieldModelType} from '../model/field-model-type';
import {FieldModel} from '../model/field-model';

@Service()
export class FieldService {
	isBlank(field: Field): boolean {
		return field.value === undefined || field.value === null || field.value === '';
	}

	isDate(fieldModel: FieldModel): boolean {
		return fieldModel.withYears;
	}

	isTime(fieldModel: FieldModel): boolean {
		return fieldModel.withHours;
	}

	generateDateFormat(fieldModel: FieldModel): string {
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

	generateTimeFormat(fieldModel: FieldModel): string {
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

	generateFormat(fieldModel: FieldModel): string {
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
