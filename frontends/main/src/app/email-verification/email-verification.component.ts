import {ChangeDetectionStrategy, Component, effect, input, signal} from '@angular/core';
import {UserService} from '@core/services/user.service';
import {Router} from '@angular/router';
import {NotificationService} from '../services/notification.service';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	selector: 'app-email-verification',
	templateUrl: './email-verification.component.html',
	styleUrl: './email-verification.component.css'
})
export class EmailVerificationComponent {
	readonly verificationCode = input.required<string>();

	readonly emailChangedSuccessfully = signal<boolean | undefined>(undefined);

	constructor(private userService: UserService,
		private notificationService: NotificationService,
		private router: Router
	) {
		effect(() => {
			this.emailChangedSuccessfully.set(undefined);
			this.userService.verifyUserEmail(this.verificationCode()).subscribe({
				next: () => {
					this.emailChangedSuccessfully.set(true);
					this.notificationService.showSuccess('Email changed, redirecting to the login page...');
					setTimeout(() => {
						this.router.navigate(['/login']);
					}, 4000);
				},
				error: err => {
					this.emailChangedSuccessfully.set(false);
					this.notificationService.showError(err.error.message);
				}
			});
		});
	}
}
