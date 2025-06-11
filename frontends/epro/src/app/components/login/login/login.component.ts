import { Component, OnDestroy, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LoadingController, AlertController, IonicModule } from '@ionic/angular';
import { Subject } from 'rxjs';
import { PublicStudyDTO } from 'src/app/api/model/public-study-dto';
import { ConfigurationService } from 'src/app/api/services/configuration.service';
import { AuthStateService } from 'src/app/services/auth-state.service';
import { LocalizerPipe } from '../../../pipes/localizer.pipe';

@Component({
	templateUrl: './login.component.html',
	styleUrls: ['./login.component.css'],
	standalone: true,
	imports: [
		IonicModule,
		RouterLink,
		FormsModule,
		ReactiveFormsModule,
		LocalizerPipe
	]
})
export class LoginComponent implements OnInit, OnDestroy {
	CODE_REGEX = /[a-z0-9]{8}/;

	study: PublicStudyDTO;

	loginForm: UntypedFormGroup;

	unsubscribe$ = new Subject<void>();

	constructor(
		private router: Router,
		private configurationService: ConfigurationService,
		private authStateService: AuthStateService,
		private formBuilder: UntypedFormBuilder,
		private loadingCtrl: LoadingController,
		private alertCtrl: AlertController
	) {
		this.loginForm = this.formBuilder.group({
			accessCode: ['', Validators.pattern(this.CODE_REGEX)],
		});
	}

	ngOnInit() {
		this.configurationService.getPublicStudy().subscribe(study => this.study = study);
	}

	async login() {
		const loader = await this.loadingCtrl.create({message: 'Please wait...'});
		loader.present();

		this.authStateService.robotLogin(this.loginForm.controls.accessCode.value).subscribe(
			() => {
				loader.dismiss();
				this.router.navigate(['/main/surveys']);
			},
			async response => {
				loader.dismiss();
				let header;
				let message;
				if (response.status === 400) {
					header = 'Invalid code';
					message = 'Check the code or contact support to ask for a new code';
				}
				else {
					header = 'Error';
					message = 'Please, try again in a few minutes';
				}
				const alert = await this.alertCtrl.create({ header, message, buttons: ['OK'] });
				alert.present();
			}
		);
	}

	ngOnDestroy() {
		this.unsubscribe$.next();
		this.unsubscribe$.complete();
	}
}
