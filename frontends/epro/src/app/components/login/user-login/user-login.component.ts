import {Component, OnInit, inject, signal} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {MatToolbar} from '@angular/material/toolbar';
import {MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatProgressBar} from '@angular/material/progress-bar';
import {MatDialog} from '@angular/material/dialog';
import {AuthStateService} from '../../../services/auth-state.service';
import {ConfirmDialogComponent} from '../../../dialogs/confirm/confirm.dialog';

@Component({
	templateUrl: './user-login.component.html',
	styleUrls: ['./user-login.component.css'],
	imports: [
		MatToolbar,
		MatButton,
		MatIcon,
		MatFormField,
		MatLabel,
		MatInput,
		MatProgressBar,
		ReactiveFormsModule
	]
})
export class UserLoginComponent implements OnInit {
	private router = inject(Router);
	private authStateService = inject(AuthStateService);
	private dialog = inject(MatDialog);

	readonly loading = signal(false);

	readonly loginForm = new FormGroup({
		email: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.email]}),
		password: new FormControl('', {nonNullable: true, validators: [Validators.required]})
	});

	ngOnInit() {
		//ensure no patient authKey is present
		this.authStateService.deleteUserToken();
	}

	login() {
		this.loading.set(true);
		const {email, password} = this.loginForm.getRawValue();

		this.authStateService.userLogin(email, password).pipe(
			finalize(() => this.loading.set(false))
		).subscribe({
			next: () => this.router.navigate(['/main/surveys']),
			error: () => this.dialog.open(ConfirmDialogComponent, {
				data: {title: 'Unable to sign in', message: 'Please, try again in a few minutes'}
			})
		});
	}
}
