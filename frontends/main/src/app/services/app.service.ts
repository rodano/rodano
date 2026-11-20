import {Service} from '@angular/core';
import {environment} from '../../environments/environment';

@Service()
export class AppService {
	public isDevMode() {
		return !environment.production;
	}
}
