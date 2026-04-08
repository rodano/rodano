import {ChangeDetectionStrategy, Component, effect, input, signal} from '@angular/core';
import {Validators, ReactiveFormsModule, FormControl, FormGroup} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {PrivacyPolicy} from '@core/model/privacy-policy';
import {ActivationService} from '@core/services/activation.service';
import {CustomValidators} from '../utils/custom-validators';
import {LocalizeMapPipe} from '../pipes/localize-map.pipe';
import {MatButton} from '@angular/material/button';
import {MatInput} from '@angular/material/input';
import {MatFormField} from '@angular/material/form-field';
import {NotificationService} from '../services/notification.service';
import {getPasswordErrorMessage} from '@core/utilities/error-utils';
import {HttpErrorResponse} from '@angular/common/http';
import {RegistrationStep} from '../registration/registration-step';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	templateUrl: './registration.component.html',
	styleUrls: ['./registration.component.css'],
	imports: [
		ReactiveFormsModule,
		MatFormField,
		MatInput,
		MatButton,
		RouterLink,
		LocalizeMapPipe
	]
})
export class RegistrationComponent {
	readonly registrationCode = input.required<string>();

	readonly step = signal<RegistrationStep>(RegistrationStep.LOADING);
	RegistrationStep = RegistrationStep;
	readonly loading = signal(false);

	readonly policies = signal<PrivacyPolicy[]>([]);
	readonly email = signal('');

	passwordForm = new FormGroup({
		email: new FormControl('', {
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

	constructor(
		private router: Router,
		private activationService: ActivationService,
		private notificationService: NotificationService
	) {
		effect(() => {
			this.activationService.getPrivacyPolicies(this.registrationCode()).subscribe({
				next: userPrivacyPolicies => {
					this.passwordForm.controls.email.setValue(userPrivacyPolicies.email);
					this.email.set(userPrivacyPolicies.email);
					if(userPrivacyPolicies.policies.length > 0) {
						this.policies.set(userPrivacyPolicies.policies);
						this.step.set(RegistrationStep.POLICIES);
					}
					else {
						this.step.set(RegistrationStep.PASSWORD);
					}
				},
				error: () => {
					this.step.set(RegistrationStep.ERROR);
					this.notificationService.showError('Invalid activation code');
				}
			});
		});
	}

	agreePolicies() {
		this.step.set(RegistrationStep.PASSWORD);
	}

	declinePolicies() {
		this.router.navigate(['/login']);
	}

	activate() {
		const password = this.passwordForm.controls.password.value;

		this.activationService.activateRole(this.registrationCode(), password).subscribe({
			next: () => {
				this.step.set(RegistrationStep.CONFIRMATION);
			},
			error: err => {
				this.notificationService.showError(getPasswordErrorMessage(err as HttpErrorResponse));
			}
		});
	}
}
