import {Component, DestroyRef, OnInit, signal} from '@angular/core';
import {FormControl, FormGroup, Validators, ReactiveFormsModule} from '@angular/forms';
import {Router, ActivatedRoute} from '@angular/router';
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
import {LocalizeMapPipe} from '../pipes/localize-map.pipe';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';

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
		LocalizeMapPipe
	]
})
export class LoginComponent implements OnInit {
	readonly study = signal<PublicStudy | undefined>(undefined);
	readonly loading = signal(false);

	readonly display = signal<LoginDisplay>(LoginDisplay.LOGIN); //Initialize to show login
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

	readonly error = signal<string | undefined>(undefined);

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
		this.configurationService.getPublicStudy().subscribe(study => this.study.set(study));
		this.returnUrl = this.activatedRoute.snapshot.queryParams['returnUrl'] || '';
	}

	login() {
		this.loading.set(true);
		const credentials = this.loginForm.value as Credentials;

		this.authStateService.login(credentials).pipe(
			takeUntilDestroyed(this.destroyRef),
			finalize(() => this.loading.set(false))
		).subscribe(
			{
				next: () => {
					this.router.navigate([this.returnUrl]);
				},
				error: (response: any) => {
					this.error.set(response.error.message);
				}
			}
		);
	}

	sendPassword() {
		this.loading.set(true);
		const email: string = this.recoveryForm.value.email ?? '';

		this.authService.recoverPassword(email).pipe(
			takeUntilDestroyed(this.destroyRef),
			finalize(() => this.loading.set(false))
		).subscribe(() => {
			this.display.set(LoginDisplay.LOGIN);
			this.router.navigate(['/login']);
			this.notificationService.showSuccess('Recovery instructions sent to the email provided');
		});
	}

	toggleDisplay() {
		if(this.display() === LoginDisplay.LOGIN) {
			this.display.set(LoginDisplay.RECOVER);
		}
		else {(this.display.set(LoginDisplay.LOGIN));}
	}
}
