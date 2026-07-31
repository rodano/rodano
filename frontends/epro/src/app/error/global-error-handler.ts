import {HttpErrorResponse} from '@angular/common/http';
import {ErrorHandler, Injector, Service, inject} from '@angular/core';
import {NotificationService} from '../services/notification.service';

@Service()
export class GlobalErrorHandler implements ErrorHandler {
	//the notification service is retrieved lazily to avoid a circular dependency with the injector during bootstrap
	private injector = inject(Injector);

	handleError(error: HttpErrorResponse) {
		if(error.status === 400 || error.status === 500) {
			this.injector.get(NotificationService).showError('Something went wrong, could not perform the operation');
		}

		console.error(error.message);
	}
}
