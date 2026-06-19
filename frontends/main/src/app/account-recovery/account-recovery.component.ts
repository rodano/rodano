import {Component, effect, input, signal} from '@angular/core';
import {Router} from '@angular/router';
import {NotificationService} from '../services/notification.service';
import {UserService} from '@core/services/user.service';

@Component({
	selector: 'app-account-recovery',
	templateUrl: './account-recovery.component.html',
	styleUrl: './account-recovery.component.css'
})
export class AccountRecoveryComponent {
	readonly recoveryCode = input.required<string>();
	readonly accountRecoverySuccessful = signal<boolean | undefined>(undefined);

	constructor(
		private userService: UserService,
		private router: Router,
		private notificationService: NotificationService
	) {
		effect(() => {
			this.userService.recoverAccount(this.recoveryCode()).subscribe({
				next: () => {
					this.notificationService.showSuccess('Account recovered, redirecting to the login page...');
					this.accountRecoverySuccessful.set(true);
					setTimeout(() => {
						this.router.navigate(['/login']);
					}, 4000);
				},
				error: error => {
					this.accountRecoverySuccessful.set(false);
					this.notificationService.showError(error.error.message);
				}
			});
		});
	}
}
