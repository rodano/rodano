import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { AlertController, LoadingController, IonicModule } from '@ionic/angular';
import { AuthStateService } from 'src/app/services/auth-state.service';
import QrScanner from 'qr-scanner';

@Component({
	templateUrl: './login-qrcode.component.html',
	styleUrls: ['./login-qrcode.component.css'],
	standalone: true,
	imports: [IonicModule]
})
export class LoginQrcodeComponent implements OnInit, OnDestroy {

	qrScanner: QrScanner;

	loading: boolean;

	constructor(
		private alertCtrl: AlertController,
		private router: Router,
		private authStateService: AuthStateService,
		private loadingCtrl: LoadingController,
	) { }

	async ngOnInit() {
		this.loading = false;

		// Get the video element
		const videoElem = document.getElementById('qr-scanner') as HTMLVideoElement;

		// Create the QR scanner
		this.qrScanner = new QrScanner(videoElem, async (result) => {
			await this.onScanSuccess(result);
		});

		// Start the scanner
		this.qrScanner.start();
	}

	async onScanSuccess(authURL: string) {
		if(!this.loading) {
			// Stop the scanner
			this.qrScanner.stop();

			this.loading = true;

			if(window.navigator.vibrate) {
				window.navigator.vibrate(200);
			}

			const url = new URL(authURL);
			const code = url.searchParams.get('code') as string;

			const loader = await this.loadingCtrl.create({message: 'Please wait...'});
			loader.present();

			this.authStateService.robotLogin(code).subscribe(
				() => {
					// Destroy the scanner
					this.qrScanner.destroy();

					this.loading = false;
					loader.dismiss();
					this.router.navigate(['/main/surveys']);
				},
				async response => {
					this.loading = false;
					loader.dismiss();
					let header;
					let message;
					if (response.status === 400) {
						header = 'Invalid code';
						message = 'Ask for a new invitation';
					}
					else {
						header = 'Error';
						message = 'Please, try again in a few minutes';
					}
					const alert = await this.alertCtrl.create({ header, message, buttons: ['OK'] });
					await alert.present();

					// Start the scanner again
					this.qrScanner.start();
				}
			);
		}
	}

	async onScanError() {
		const alert = await this.alertCtrl.create({
			header: 'Could not scan the QR code',
			message: 'Please try again',
			buttons: ['OK']
		});
		alert.present();
	}


	ngOnDestroy(): void {
		if(this.qrScanner) {
			this.qrScanner.stop();
			this.qrScanner.destroy();
		}
	}
}
