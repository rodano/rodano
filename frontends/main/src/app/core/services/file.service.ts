import {Injectable} from '@angular/core';
import {HttpClient, HttpRequest, HttpEvent} from '@angular/common/http';
import {APIService} from './api.service';
import {Observable} from 'rxjs';
import {FileModel} from '../model/file-model';

@Injectable({
	providedIn: 'root'
})
export class FileService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) { }

	upload(scopePk: number, file: File, eventPk?: number): Observable<HttpEvent<FileModel>> {
		const formData = new FormData();
		formData.append('file', file, file.name);

		const url = eventPk
			? `${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/files`
			: `${this.apiService.getApiUrl()}/scopes/${scopePk}/files`;

		const request = new HttpRequest('POST', url, formData, {reportProgress: true});
		return this.http.request(request);
	}

	download(filePk: number): Observable<Blob> {
		return this.http.get(`${this.apiService.getApiUrl()}/files/${filePk}`, {responseType: 'blob'});
	}

	getUrl(filePk: number): string {
		return `${this.apiService.getApiUrl()}/files/${filePk}`;
	}
}
