import {HttpErrorResponse} from '@angular/common/http';
import {Component, effect, input, signal} from '@angular/core';
import {FormGroup, FormControl, Validators, ReactiveFormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {AuthService} from '@core/services/auth.service';
import {NotificationService} from '../services/notification.service';
import {CustomValidators} from '../utils/custom-validators';
import {MatButton} from '@angular/material/button';
import {MatInput} from '@angular/material/input';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatCard, MatCardActions, MatCardContent, MatCardHeader, MatCardSubtitle, MatCardTitle} from '@angular/material/card';
import {getPasswordErrorMessage} from '@core/utilities/error-utils';
import {ChangePasswordContext} from './change-password-context';
import {ResetPassword} from '@core/model/reset-password';
import {ChangePassword} from '@core/model/change-password';
import {User} from '@core/model/user';

@Component({
	selector: 'app-change-password',
	templateUrl: './change-password.component.html',
	styleUrls: ['./change-password.component.css'],
	imports: [
		ReactiveFormsModule,
		MatCard,
		MatCardHeader,
		MatCardTitle,
		MatCardSubtitle,
		MatCardContent,
		MatCardActions,
		MatLabel,
		MatFormField,
		MatInput,
		MatButton
	]
})
export class ChangePasswordComponent {
	readonly loading = signal(false);

	readonly me = input<User>(); //me will be available when the user is logged in
	readonly recoveryCode = input<string>(); //recoveryCode will be available when the user clicks on the link in password recovery email
	readonly changeRequestContext = input.required<ChangePasswordContext>();

	ChangePasswordContext = ChangePasswordContext;

	changePasswordForm = new FormGroup({
		email: new FormControl(''),
		currentPassword: new FormControl('', {
			nonNullable: true
		}),
		password: new FormControl('', {
			nonNullable: true,
			validators: [Validators.required]
		}),
		confirmPassword: new FormControl('', {
			nonNullable: true,
			validators: [Validators.required]
		})
	}, {validators: CustomValidators.matchingPasswords});

	readonly error = signal<string | undefined>(undefined);

	constructor(
		private authService: AuthService,
		private router: Router,
		private notificationService: NotificationService
	) {
		effect(() => {
			const email = this.me()?.email;
			if(email) {
				this.changePasswordForm.get('email')?.setValue(email);
			}
		});

		effect(() => {
			const currentPasswordControl = this.changePasswordForm.get('currentPassword');
			if(this.recoveryCode()) {
				currentPasswordControl?.clearValidators();
			}
			else {
				currentPasswordControl?.setValidators(Validators.required);
			}
			currentPasswordControl?.updateValueAndValidity();
		});
	}

	updatePassword(): void {
		this.loading.set(true);

		if(this.recoveryCode() && this.changeRequestContext() === ChangePasswordContext.PASSWORD_RESET) {
			const newPassword = this.changePasswordForm.controls.password.value;
			const resetPassword = {} as ResetPassword;
			resetPassword.newPassword = newPassword;
			resetPassword.resetCode = this.recoveryCode()!;
			this.authService.resetPassword(resetPassword).pipe(
				finalize(() => this.loading.set(false))
			).subscribe({
				next: () => {
					this.notificationService.showSuccess('Password reset');
					this.router.navigate(['/login']);
				},
				error: (response: any) => {
					this.error.set(getPasswordErrorMessage(response as HttpErrorResponse, this.recoveryCode() !== undefined));
				}
			});
		}
		else {
			const changePassword = {} as ChangePassword;
			changePassword.currentPassword = this.changePasswordForm.controls.currentPassword.value;
			changePassword.newPassword = this.changePasswordForm.controls.password.value;

			this.authService.changePassword(changePassword).pipe(
				finalize(() => {
					this.loading.set(false);
				})
			).subscribe({
				next: () => {
					if(this.changeRequestContext() === ChangePasswordContext.USER_REQUEST) {
						this.error.set('');
						this.changePasswordForm.reset();
					}
					else if(this.changeRequestContext() === ChangePasswordContext.SYSTEM_REQUEST) {
						this.router.navigate(['/dashboard']);
					}
					this.notificationService.showSuccess('Password changed');
				},
				error: (response: any) => {
					this.error.set(getPasswordErrorMessage(response as HttpErrorResponse, this.recoveryCode() !== undefined));
				}
			});
		}
	}
}
