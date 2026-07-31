import {Component, inject, signal} from '@angular/core';
import {Router} from '@angular/router';
import {finalize} from 'rxjs/operators';
import {MatButton} from '@angular/material/button';
import {ConfigurationService} from '@core/services/configuration.service';

@Component({
	templateUrl: './offline.component.html',
	styleUrls: ['./offline.component.css'],
	imports: [MatButton]
})
export class OfflineComponent {
	private configService = inject(ConfigurationService);
	private router = inject(Router);

	readonly loading = signal(false);

	checkConnectionAndProceed() {
		this.loading.set(true);
		//try to get the public study, a failure means the server is still unreachable
		this.configService.getPublicStudy().pipe(
			finalize(() => this.loading.set(false))
		).subscribe({
			next: () => this.router.navigate(['/']),
			error: () => undefined
		});
	}
}
