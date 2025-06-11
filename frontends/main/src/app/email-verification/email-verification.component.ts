import {Component, Input, OnChanges} from '@angular/core';
import {UserService} from '@core/services/user.service';
import {Router} from '@angular/router';
import {NotificationService} from '../services/notification.service';

@Component({
	selector: 'app-email-verification',
	templateUrl: './email-verification.component.html',
	styleUrl: './email-verification.component.css'
})
export class EmailVerificationComponent implements OnChanges {
	@Input() verificationCode: string;

	emailChangedSuccessfully: boolean | undefined = undefined;

	constructor(private userService: UserService,
		private notificationService: NotificationService,
		private router: Router
	) {}

	ngOnChanges(): void {
		this.emailChangedSuccessfully = undefined;
		this.userService.verifyUserEmail(this.verificationCode).subscribe({
			next: () => {
				this.emailChangedSuccessfully = true;
				this.notificationService.showSuccess('Email changed, redirecting to the login page...');
				setTimeout(() => {
					this.router.navigate(['/login']);
				}, 4000);
			},
			error: err => {
				this.emailChangedSuccessfully = false;
				this.notificationService.showError(err.error.message);
			}
		});
	}
}
