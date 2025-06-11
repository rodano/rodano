import {AbstractControl, ValidationErrors, ValidatorFn} from '@angular/forms';

export const nonEmptyArrayValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
	const value = control.value;
	return Array.isArray(value) && value.length > 0 ? null : {nonEmptyArray: true};
};
