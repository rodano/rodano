import {HttpErrorResponse} from '@angular/common/http';
import {Component, Input, OnInit} from '@angular/core';
import {FormGroup, FormControl, Validators, ReactiveFormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {AuthService} from '@core/services/auth.service';
import {NotificationService} from '../services/notification.service';
import {CustomValidators} from '../utils/custom-validators';
import {MatButton} from '@angular/material/button';
import {MatInput} from '@angular/material/input';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatCardModule} from '@angular/material/card';
import {getPasswordErrorMessage} from '@core/utilities/error-utils';
import {ChangePasswordContext} from './change-password-context';
import {ResetPasswordDTO} from '@core/model/reset-password-dto';
import {ChangePasswordDTO} from '@core/model/change-password-dto';
import {UserDTO} from '@core/model/user-dto';

@Component({
	selector: 'app-change-password',
	templateUrl: './change-password.component.html',
	styleUrls: ['./change-password.component.css'],
	imports: [
		MatCardModule,
		ReactiveFormsModule,
		MatLabel,
		MatFormField,
		MatInput,
		MatButton
	]
})
export class ChangePasswordComponent implements OnInit {
	loading = false;

	@Input() me?: UserDTO; //me will be available when the user is logged in
	@Input() recoveryCode?: string; //recoveryCode will be available when the user clicks on the link in password recovery email
	@Input() changeRequestContext: ChangePasswordContext;

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

	errorText: string;

	constructor(
		private authService: AuthService,
		private router: Router,
		private notificationService: NotificationService
	) {}

	ngOnInit(): void {
		if(this.me) {
			this.changePasswordForm.get('email')?.setValue(this.me.email);
		}
		if(!this.recoveryCode) {
			this.changePasswordForm.get('currentPassword')?.setValidators(Validators.required);
		}
	}

	updatePassword(): void {
		this.loading = true;

		if(this.recoveryCode && this.changeRequestContext === ChangePasswordContext.PASSWORD_RESET) {
			const newPassword = this.changePasswordForm.controls.password.value;
			const resetPasswordDTO = {} as ResetPasswordDTO;
			resetPasswordDTO.newPassword = newPassword;
			resetPasswordDTO.resetCode = this.recoveryCode;
			this.authService.resetPassword(resetPasswordDTO).pipe(
				finalize(() => this.loading = false)
			).subscribe({
				next: () => {
					this.notificationService.showSuccess('Password reset');
					this.router.navigate(['/login']);
				},
				error: (response: any) => {
					this.errorText = getPasswordErrorMessage(response as HttpErrorResponse, this.recoveryCode !== undefined);
				}
			});
		}
		else {
			const changePasswordDTO = {} as ChangePasswordDTO;
			changePasswordDTO.currentPassword = this.changePasswordForm.controls.currentPassword.value;
			changePasswordDTO.newPassword = this.changePasswordForm.controls.password.value;

			this.authService.changePassword(changePasswordDTO).pipe(
				finalize(() => {
					this.loading = false;
				})
			).subscribe({
				next: () => {
					if(this.changeRequestContext === ChangePasswordContext.USER_REQUEST) {
						this.errorText = '';
						this.changePasswordForm.reset();
					}
					else if(this.changeRequestContext === ChangePasswordContext.SYSTEM_REQUEST) {
						this.router.navigate(['/dashboard']);
					}
					this.notificationService.showSuccess('Password changed');
				},
				error: (response: any) => {
					this.errorText = getPasswordErrorMessage(response as HttpErrorResponse, this.recoveryCode !== undefined);
				}
			});
		}
	}
}
