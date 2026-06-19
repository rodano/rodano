import {Service, inject} from '@angular/core';
import {CanActivate, Router} from '@angular/router';
import {ConfigurationService} from '@core/services/configuration.service';
import {map} from 'rxjs/operators';
import {Observable} from 'rxjs';

@Service()
export class EproEnabledGuard implements CanActivate {
	private readonly configurationService = inject(ConfigurationService);
	private readonly router = inject(Router);

	canActivate(): Observable<boolean> {
		return this.configurationService.getStudy().pipe(
			map(study => {
				if(study.eproEnabled) {
					return true;
				}
				else {
					this.router.navigate(['/']);
					return false;
				}
			})
		);
	}
}
