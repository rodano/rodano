import {Injectable} from '@angular/core';

@Injectable({
	providedIn: 'root'
})
export class APIService {
	private static API_URL = '/api';

	private apiUrl = APIService.API_URL;

	public getApiUrl(): string {
		return this.apiUrl;
	}

	public setApiUrl(url: string) {
		this.apiUrl = url;
	}
}
