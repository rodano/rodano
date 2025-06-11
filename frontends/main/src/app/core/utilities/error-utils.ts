import {HttpErrorResponse} from '@angular/common/http';

export const getPasswordErrorMessage = (
	error: HttpErrorResponse,
	passwordResetCodePresent?: boolean
): string => {
	switch(error.status) {
		case 401:
			return passwordResetCodePresent ? 'Password reset code is invalid' : 'Wrong password';
		case 404:
			return 'The password reset link you used is invalid or has expired. Please ask for another one.';
		case 400:
		case 412:
			return error.error.message;
		default:
			return `${error.error.message} - You may contact support.`;
	}
};
