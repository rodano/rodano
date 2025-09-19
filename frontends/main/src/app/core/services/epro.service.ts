import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {APIService} from './api.service';
import {EPROInvitation} from '../model/epro-invitation';
import {EproRobot} from '../model/epro-robot';

@Injectable({
	providedIn: 'root'
})
export class EproService {
	private serviceUrl: string;

	constructor(
		private http: HttpClient,
		private apiService: APIService
	) {
		this.serviceUrl = `${this.apiService.getApiUrl()}/epro`;
	}

	getInvitedRobots(): Observable<EproRobot[]> {
		return this.http.get<EproRobot[]>(`${this.serviceUrl}/robots`);
	}

	getRobot(key: string): Observable<EproRobot> {
		return this.http.post<EproRobot>(`${this.serviceUrl}/robot`, {key});
	}

	invite(scopePk: number): Observable<EPROInvitation> {
		return this.http.put<EPROInvitation>(`${this.serviceUrl}/${scopePk}/invite`, undefined);
	}

	revoke(scopePk: number): Observable<void> {
		return this.http.put<void>(`${this.serviceUrl}/${scopePk}/revoke`, undefined);
	}
}
