import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {UserPasswordDialogResult} from './user-password-dialog-result';
import {MatButton} from '@angular/material/button';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatInput} from '@angular/material/input';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {UserDTO} from '@core/model/user-dto';

@Component({
	selector: 'app-user-password',
	templateUrl: './user-password.dialog.html',
	imports: [
		ReactiveFormsModule,
		MatDialogModule,
		MatFormField,
		MatLabel,
		MatInput,
		MatButton
	]
})
export class UserPasswordDialogComponent {
	passwordForm = new FormGroup({
		email: new FormControl('', {
			nonNullable: true,
			validators: [Validators.required, Validators.email]
		}),
		password: new FormControl('', {
			nonNullable: true,
			validators: [Validators.required]
		})
	});

	constructor(
		@Inject(MAT_DIALOG_DATA) public data: {actionLabel: string; user: UserDTO}
	) { }

	getResponse(): UserPasswordDialogResult {
		return this.passwordForm.value as UserPasswordDialogResult;
	}
}
