import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AlertController, LoadingController } from '@ionic/angular';
import { AuthStateService } from 'src/app/services/auth-state.service';

@Component({
	templateUrl: './user-login.component.html',
	styleUrls: ['./user-login.component.css']
})
export class UserLoginComponent implements OnInit {

	public loginForm: FormGroup;

	constructor(
		private formBuilder: FormBuilder,
		private alertController: AlertController,
		private router: Router,
		private authStateService: AuthStateService,
		private loadingCtrl: LoadingController
	) {
		this.loginForm = this.formBuilder.group({
			email: ['', Validators.compose([Validators.required, Validators.email])],
			password: ['', Validators.required],
		});
	}

	ngOnInit() {
		// Ensure no patient authKey is present
		this.authStateService.deleteUserToken();
	}

	public async login() {
		// Disable user interaction
		const loader = await this.loadingCtrl.create({ message: 'Please wait...' });
		loader.present();

		const credentials = {
			email: this.loginForm.controls.email.value,
			password: this.loginForm.controls.password.value
		};

		// Login to KV
		this.authStateService.userLogin(credentials.email, credentials.password).subscribe(
			() => {
				loader.dismiss();
				this.router.navigate(['/main/surveys']);
			},
			async () => {
				loader.dismiss();
				// Use message for time being as done in KV
				const header = 'Unable to sign in';
				const message = 'Please, try again in a few minutes';
				const alert = await this.alertController.create({ header, message, buttons: ['OK'] });
				alert.present();
			}
		);
	}

	public lostPassword() {
		console.log('Lost Password');
	}

}
