import {Service} from '@angular/core';
import {environment} from 'src/environments/environment';

@Service()
export class AppService {
	public isDevMode() {
		return !environment.production;
	}
}
