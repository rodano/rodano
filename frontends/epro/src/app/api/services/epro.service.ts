import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EPROInvitation } from '../model/epro-invitation-dto';
import { APIService } from './api.service';

@Injectable({
	providedIn: 'root'
})
export class EproService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) { }

	getInvited(): Observable<EPROInvitation[]> {
		return this.http.get<EPROInvitation[]>(`${this.apiService.getApiUrl()}/epro/management/invited`);
	}

	invite(scopePk: number): Observable<EPROInvitation> {
		return this.http.put<EPROInvitation>(`${this.apiService.getApiUrl()}/epro/management/${scopePk}/invite`, undefined);
	}

	uninvite(scopePk: number): Observable<void> {
		return this.http.put<void>(`${this.apiService.getApiUrl()}/epro/management/${scopePk}/uninvite`, undefined);
	}

	getInvitationDetails(scopePk: number): Observable<EPROInvitation> {
		return this.http.get<EPROInvitation>(`${this.apiService.getApiUrl()}/epro/management/details/${scopePk}`);
	}

}
