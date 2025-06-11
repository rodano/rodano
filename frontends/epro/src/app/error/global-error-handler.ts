import { HttpErrorResponse } from '@angular/common/http';
import { ErrorHandler, Injectable } from '@angular/core';
import { ToastController } from '@ionic/angular';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
	constructor(
		private toastCtrl: ToastController
	) {}

	handleError(error: HttpErrorResponse) {
		if(error.status === 400 || error.status === 500) {
			this.presentErrorToast();
		}

		console.error(error.message);
	}

	private async presentErrorToast() {
		const toast = await this.toastCtrl.create({
			position: 'top',
			header: 'Something went wrong',
			message: 'Could not perform the operation',
			color: 'danger',
			duration: 3000
		});

		toast.present();
	}
}
