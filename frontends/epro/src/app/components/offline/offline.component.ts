import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ConfigurationService } from 'src/app/api/services/configuration.service';
import { IonicModule } from '@ionic/angular';

@Component({
	templateUrl: './offline.component.html',
	standalone: true,
	imports: [IonicModule]
})
export class OfflineComponent {

	loading = false;

	constructor(
		private configService: ConfigurationService,
		private router: Router
	) { }

	checkConnectionAndProceed(): void {
		this.loading = true;
		// Try to get the public study and if we get a 504, the operation fails.
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
