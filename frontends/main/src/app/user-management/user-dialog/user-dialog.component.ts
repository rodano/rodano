import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatDialogRef, MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {UserManagement} from '@core/model/user-management';
import {UserManagementService} from '@core/services/user-management.service';
import {CreateUserRequest} from '@core/model/create-user-request';
import {UpdateUserRequest} from '@core/model/update-user-request';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MatIcon} from '@angular/material/icon';

export interface UserDialogData {
	mode: 'create' | 'edit';
	user?: UserManagement;
}

@Component({
	selector: 'app-user-dialog',
	standalone: true,
	templateUrl: './user-dialog.component.html',
	styleUrls: ['./user-dialog.component.css'],
	imports: [
		CommonModule,
		MatDialogModule,
		MatButtonModule,
		MatProgressSpinner,
		ReactiveFormsModule,
		MatIcon
	]
})
export class UserDialogComponent implements OnInit {
	userForm: FormGroup;
	isEditMode: boolean;
	loading = false;

	availableLanguages = [
		{code: 'en', name: 'English'},
		{code: 'de', name: 'German'},
		{code: 'fr', name: 'French'},
		{code: 'it', name: 'Italian'}
	];

	constructor(
		private fb: FormBuilder,
		private userManagementService: UserManagementService,
		private dialogRef: MatDialogRef<UserDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: UserDialogData
	) {
		this.isEditMode = data.mode === 'edit';
	}

	ngOnInit(): void {
		this.initForm();
	}

	initForm(): void {
		if(this.isEditMode && this.data.user) {
			//Edit mode - populate with existing user data
			this.userForm = this.fb.group({
				name: [this.data.user.name, [Validators.required, Validators.minLength(2)]],
				email: [{value: this.data.user.email, disabled: true}, [Validators.required, Validators.email]],
				languageId: [this.data.user.languageId, Validators.required],
				phone: [this.data.user.phone || ''],
				isSuperuser: [this.data.user.isSuperuser]
			});
		}
		else {
			//Create mode
			this.userForm = this.fb.group({
				name: ['', [Validators.required, Validators.minLength(2)]],
				email: ['', [Validators.required, Validators.email]],
				languageId: ['en', Validators.required],
				phone: [''],
				isSuperuser: [false],
				sendActivationEmail: [true]
			});
		}
	}

	onSubmit(): void {
		if(this.userForm.invalid) {
			this.userForm.markAllAsTouched();
			return;
		}

		this.loading = true;

		if(this.isEditMode) {
			this.updateUser();
		}
		else {
			this.createUser();
		}
	}

	createUser(): void {
		const request: CreateUserRequest = {
			name: this.userForm.value.name,
			email: this.userForm.value.email,
			languageId: this.userForm.value.languageId,
			phone: this.userForm.value.phone || null,
			isSuperuser: this.userForm.value.isSuperuser || false,
			sendActivationEmail: this.userForm.value.sendActivationEmail || false
		};

		this.userManagementService.createUser(request).subscribe({
			next: user => {
				this.loading = false;
				this.dialogRef.close(user);
			},
			error: error => {
				this.loading = false;
				console.error('Error creating user:', error);
			}
		});
	}

	updateUser(): void {
		const request: UpdateUserRequest = {
			name: this.userForm.value.name,
			languageId: this.userForm.value.languageId,
			phone: this.userForm.value.phone || null,
			isSuperuser: this.userForm.value.isSuperuser
		};

		this.userManagementService.updateUser(this.data.user?.pk, request).subscribe({
			next: user => {
				this.loading = false;
				this.dialogRef.close(user);
			},
			error: error => {
				this.loading = false;
				console.error('Error updating user:', error);
			}
		});
	}

	onCancel(): void {
		this.dialogRef.close();
	}

	getErrorMessage(field: string): string {
		const control = this.userForm.get(field);
		if(control?.hasError('required')) {
			return 'This field is required';
		}
		if(control?.hasError('email')) {
			return 'Please enter a valid email address';
		}
		if(control?.hasError('minlength')) {
			return `Minimum length is ${control.errors?.['minlength'].requiredLength}`;
		}
		return '';
	}
}
