import {AbstractControl} from '@angular/forms';

export class CustomValidators {
	static matchingPasswords(control: AbstractControl) {
		const password = control.get('password');
		const confirmPassword = control.get('confirmPassword');

		if(password && confirmPassword) {
			return password.value === confirmPassword.value ? null : {notSame: true};
		}
		return null;
	}
}
