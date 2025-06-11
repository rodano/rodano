import {Injectable, ErrorHandler} from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';
import {NotificationService} from '../notification.service';

@Injectable({
	providedIn: 'root'
})
export class GlobalErrorHandler implements ErrorHandler {
	constructor(
		private notificationService: NotificationService
	) { }

	handleError(error: Error | HttpErrorResponse) {
		console.error(error);
		if(error instanceof Error) {
			let message;
			if(!navigator.onLine) {
				message = 'No internet connection';
			}
			else {
				message = error.message ? error.message : error.toString();
			}

			this.notificationService.showError(message);
		}
	}
}
