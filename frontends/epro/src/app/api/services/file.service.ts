import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpEvent } from '@angular/common/http';
import { APIService } from './api.service';
import { Observable } from 'rxjs';

@Injectable({
	providedIn: 'root'
})
export class FileService {

	constructor(
		private http: HttpClient,
		private apiService: APIService
	) { }

	uploadFile(uuid: string, file: File): Observable<HttpEvent<void>> {
		const formData = new FormData();
		formData.append('uuid', uuid);
		formData.append('file', file, file.name);

		const request = new HttpRequest('POST', `${this.apiService.getApiUrl()}/files`, formData, { reportProgress: true });
		return this.http.request(request);
	}

	downloadFile(uuid: string): Observable<Blob> {
		return this.http.get(`${this.apiService.getApiUrl()}/files/${uuid}`, { responseType: 'blob' });
	}

	getFileURL(uuid: string): string {
		return `${this.apiService.getApiUrl()}/files/${uuid}`;
	}

	/* eslint-disable no-bitwise */
	getNewUUID(): string {
		return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (char) => {
			const random = Math.random() * 16 | 0;
			const value = char === 'x' ? random : (random % 4 + 8);
			return value.toString(16);
		});
	}
}
