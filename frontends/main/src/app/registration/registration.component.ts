import {Component, Input, OnChanges} from '@angular/core';
import {Validators, ReactiveFormsModule, FormControl, FormGroup} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {PrivacyPolicyDTO} from '@core/model/privacy-policy-dto';
import {ActivationService} from '@core/services/activation.service';
import {CustomValidators} from '../utils/custom-validators';
import {LocalizeMapPipe} from '../pipes/localize-map.pipe';
import {MatButton} from '@angular/material/button';
import {MatInput} from '@angular/material/input';
import {MatFormField} from '@angular/material/form-field';
import {NotificationService} from '../services/notification.service';
import {getPasswordErrorMessage} from '@core/utilities/error-utils';
import {HttpErrorResponse} from '@angular/common/http';
import {RegistrationStep} from 'src/app/registration/registration-step';

@Component({
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
export class RegistrationComponent implements OnChanges {
	@Input() registrationCode: string;

	step: RegistrationStep = RegistrationStep.LOADING; //Initialize with the first step
	RegistrationStep = RegistrationStep;
	loading = false;

	policies: PrivacyPolicyDTO[];
	email: string;

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

	initLoadCompleted = false;

	constructor(
		private router: Router,
		private activationService: ActivationService,
		private notificationService: NotificationService
	) { }

	ngOnChanges() {
		this.activationService.getPrivacyPolicies(this.registrationCode).subscribe({
			next: userPrivacyPolicies => {
				this.passwordForm.controls.email.setValue(userPrivacyPolicies.email);
				this.email = userPrivacyPolicies.email;
				if(userPrivacyPolicies.policies.length > 0) {
					this.policies = userPrivacyPolicies.policies;
					this.step = RegistrationStep.POLICIES;
				}
				else {
					this.step = RegistrationStep.PASSWORD;
				}
			},
			error: () => {
				this.step = RegistrationStep.ERROR;
				this.notificationService.showError('Invalid activation code');
			}
		});
	}

	agreePolicies() {
		this.step = RegistrationStep.PASSWORD;
	}

	declinePolicies() {
		this.router.navigate(['/login']);
	}

	activate() {
		const password = this.passwordForm.controls.password.value;

		this.activationService.activateRole(this.registrationCode, password).subscribe({
			next: () => {
				this.step = RegistrationStep.CONFIRMATION;
			},
			error: err => {
				this.notificationService.showError(getPasswordErrorMessage(err as HttpErrorResponse));
			}
		});
	}
}
