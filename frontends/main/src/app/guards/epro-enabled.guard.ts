import {Injectable} from '@angular/core';
import {CanActivate, Router} from '@angular/router';
import {ConfigurationService} from '@core/services/configuration.service';
import {map} from 'rxjs/operators';
import {Observable} from 'rxjs';

@Injectable({
	providedIn: 'root'
})
export class EproEnabledGuard implements CanActivate {
	constructor(
		private configurationService: ConfigurationService,
		private router: Router
	) {}

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
