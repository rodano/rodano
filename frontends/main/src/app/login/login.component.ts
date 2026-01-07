import {Component, DestroyRef, OnInit} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {PublicStudy} from '@core/model/public-study';
import {Credentials} from '@core/model/credentials';
import {ConfigurationService} from '@core/services/configuration.service';
import {AuthStateService} from '../services/auth-state.service';
import {MatButton} from '@angular/material/button';
import {MatInput} from '@angular/material/input';
import {MatError, MatFormField, MatLabel} from '@angular/material/form-field';
import {LoginDisplay} from './login-display';
import {AuthService} from '@core/services/auth.service';
import {NotificationService} from '../services/notification.service';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {LogoComponent} from '../logo/logo.component';

@Component({
	selector: 'app-login',
	templateUrl: './login.component.html',
	styleUrls: ['./login.component.css'],
	imports: [
		ReactiveFormsModule,
		MatFormField,
		MatInput,
		MatError,
		MatLabel,
		MatButton,
		LogoComponent
	]
})
export class LoginComponent implements OnInit {
	study?: PublicStudy;
	loading = false;

	display: LoginDisplay = LoginDisplay.LOGIN; //Initialize to show login
	loginDisplay = LoginDisplay;
	returnUrl: string;

	loginForm = new FormGroup({
		email: new FormControl('', {
			nonNullable: true,
			validators: [Validators.required, Validators.email]
		}),
		password: new FormControl('', {
			nonNullable: true,
			validators: [Validators.required]
		})
	});

	recoveryForm = new FormGroup({
		email: new FormControl('', {
			nonNullable: true,
			validators: [Validators.required, Validators.email]
		})
	});

	error: string;

	constructor(
		private configurationService: ConfigurationService,
		private authStateService: AuthStateService,
		private router: Router,
		private activatedRoute: ActivatedRoute,
		private authService: AuthService,
		private notificationService: NotificationService,
		private destroyRef: DestroyRef
	) {}

	ngOnInit() {
		document.documentElement.style.removeProperty('--mat-sys-primary');

		this.configurationService.clearStudy();
		this.study = undefined;

		this.configurationService.getPublicStudy().subscribe({
			next: study => {
				this.study = study;
			},
			error: error => {
				if(error.status === 204) {
					console.log('No study loaded yet - will load after project selection');
				}
				else {
					console.error('Error loading public study:', error);
				}
				this.study = undefined;
			}
		});
		this.returnUrl = this.activatedRoute.snapshot.queryParams['returnUrl'] || '/projects';
	}

	login() {
		this.loading = true;
		const credentials = this.loginForm.value as Credentials;

		this.authStateService.login(credentials).pipe(
			takeUntilDestroyed(this.destroyRef),
			finalize(() => this.loading = false)
		).subscribe(
			{
				next: () => {
					this.router.navigate(['/projects']);
				},
				error: (response: any) => {
					this.error = response.error.message;
				}
			}
		);
	}

	sendPassword() {
		this.loading = true;
		const email: string = this.recoveryForm.value.email ?? '';

		this.authService.recoverPassword(email).pipe(
			takeUntilDestroyed(this.destroyRef),
			finalize(() => this.loading = false)
		).subscribe(() => {
			this.display = LoginDisplay.LOGIN;
			this.router.navigate(['/login']);
			this.notificationService.showSuccess('Recovery instructions sent to the email provided');
		});
	}

	toggleDisplay() {
		if(this.display === LoginDisplay.LOGIN) {
			this.display = LoginDisplay.RECOVER;
		}
		else {(this.display = LoginDisplay.LOGIN);}
	}
}
