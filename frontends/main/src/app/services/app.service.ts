import {Injectable} from '@angular/core';
import {environment} from 'src/environments/environment';

@Injectable({
	providedIn: 'root'
})
export class AppService {
	public isDevMode() {
		return !environment.production;
	}
}
