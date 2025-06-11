import { Injectable } from '@angular/core';

@Injectable()
export class APIService {
	private static API_URL = '/api';

	public getApiUrl(): string {
		return APIService.API_URL;
	}
}
