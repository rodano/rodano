import {Component, OnInit, inject, signal} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatProgressBar} from '@angular/material/progress-bar';
import {MatDialog} from '@angular/material/dialog';
import {PublicStudy} from '@core/model/public-study';
import {ConfigurationService} from '@core/services/configuration.service';
import {AuthStateService} from '../../../services/auth-state.service';
import {LocalizerPipe} from '../../../pipes/localizer.pipe';
import {ConfirmDialogComponent, ConfirmDialogData} from '../../../dialogs/confirm/confirm.dialog';

const CODE_REGEX = /[a-z0-9]{8}/;

@Component({
	templateUrl: './login.component.html',
	styleUrls: ['./login.component.css'],
	imports: [
		MatButton,
		MatIcon,
		MatFormField,
		MatLabel,
		MatInput,
		MatProgressBar,
		RouterLink,
		ReactiveFormsModule,
		LocalizerPipe
	]
})
export class LoginComponent implements OnInit {
	private router = inject(Router);
	private configurationService = inject(ConfigurationService);
	private authStateService = inject(AuthStateService);
	private dialog = inject(MatDialog);

	readonly study = signal<PublicStudy | undefined>(undefined);
	readonly loading = signal(false);

	readonly loginForm = new FormGroup({
		accessCode: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.pattern(CODE_REGEX)]})
	});

	ngOnInit() {
		this.configurationService.getPublicStudy().subscribe(study => this.study.set(study));
	}

	login() {
		this.loading.set(true);
		this.authStateService.robotLogin(this.loginForm.controls.accessCode.value).pipe(
			finalize(() => this.loading.set(false))
		).subscribe({
			next: () => this.router.navigate(['/main/surveys']),
			error: response => {
				const data: ConfirmDialogData = response.status === 400
					? {title: 'Invalid code', message: 'Check the code or contact support to ask for a new code'}
					: {title: 'Error', message: 'Please, try again in a few minutes'};
				this.dialog.open(ConfirmDialogComponent, {data});
			}
		});
	}
}
