import { Injectable } from '@angular/core';
import { ToastController } from '@ionic/angular';
import { Router } from '@angular/router';

@Injectable()
export class ErrorService {

	constructor(
		private toastCtrl: ToastController,
		private router: Router
	) {	}

}
