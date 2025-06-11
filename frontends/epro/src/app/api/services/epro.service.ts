import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EproInvitation } from '../model/epro-invitation';
import { APIService } from './api.service';
import { EproInvitationDetails } from '../model/epro-invitation-details';

@Injectable({
	providedIn: 'root'
})
export class EproService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) { }

	getInvited(): Observable<EproInvitation[]> {
		return this.http.get<EproInvitation[]>(`${this.apiService.getApiUrl()}/epro/management/invited`);
	}

	invite(scopePk: number): Observable<EproInvitationDetails> {
		return this.http.put<EproInvitationDetails>(`${this.apiService.getApiUrl()}/epro/management/${scopePk}/invite`, undefined);
	}

	uninvite(scopePk: number): Observable<void> {
		return this.http.put<void>(`${this.apiService.getApiUrl()}/epro/management/${scopePk}/uninvite`, undefined);
	}

	getInvitationDetails(scopePk: number): Observable<EproInvitationDetails> {
		return this.http.get<EproInvitationDetails>(`${this.apiService.getApiUrl()}/epro/management/details/${scopePk}`);
	}

}
