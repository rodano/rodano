import {Component} from '@angular/core';
import {Router} from '@angular/router';
import {ConfigurationService} from '@core/services/configuration.service';
import {IonButton, IonContent} from '@ionic/angular/standalone';

@Component({
	templateUrl: './offline.component.html',
	standalone: true,
	imports: [IonButton, IonContent]
})
export class OfflineComponent {
	loading = false;

	constructor(
		private configService: ConfigurationService,
		private router: Router
	) { }

	checkConnectionAndProceed(): void {
		this.loading = true;
		//Try to get the public study and if we get a 504, the operation fails.
		this.configService.getPublicStudy().subscribe(
			() => {
				this.loading = false;
				this.router.navigate(['/']);
			},
			() => {
				this.loading = false;
			}
		);
	}
}
