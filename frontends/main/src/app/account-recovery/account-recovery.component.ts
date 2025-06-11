import {Component, Input, OnChanges} from '@angular/core';
import {Router} from '@angular/router';
import {NotificationService} from '../services/notification.service';
import {UserService} from '@core/services/user.service';

@Component({
	selector: 'app-account-recovery',
	templateUrl: './account-recovery.component.html',
	styleUrl: './account-recovery.component.css'
})
export class AccountRecoveryComponent implements OnChanges {
	@Input() recoveryCode: string;
	accountRecoverySuccessful: boolean | undefined = undefined;

	constructor(
		private userService: UserService,
		private router: Router,
		private notificationService: NotificationService
	) {}

	ngOnChanges(): void {
		this.userService.recoverAccount(this.recoveryCode).subscribe({
			next: () => {
				this.notificationService.showSuccess('Account recovered, redirecting to the login page...');
				this.accountRecoverySuccessful = true;
				setTimeout(() => {
					this.router.navigate(['/login']);
				}, 4000);
			},
			error: err => {
				this.accountRecoverySuccessful = false;
				this.notificationService.showError(err.error.message);
			}
		});
	}
}
