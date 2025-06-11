import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {APIService} from './api.service';
import {EPROInvitationDTO} from '../model/epro-invitation-dto';
import {EproRobotDTO} from '../model/epro-robot-dto';

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

	getInvitedRobots(): Observable<EproRobotDTO[]> {
		return this.http.get<EproRobotDTO[]>(`${this.serviceUrl}/robots`);
	}

	getRobot(key: string): Observable<EproRobotDTO> {
		return this.http.post<EproRobotDTO>(`${this.serviceUrl}/robot`, {key});
	}

	invite(scopePk: number): Observable<EPROInvitationDTO> {
		return this.http.put<EPROInvitationDTO>(`${this.serviceUrl}/${scopePk}/invite`, undefined);
	}

	revoke(scopePk: number): Observable<void> {
		return this.http.put<void>(`${this.serviceUrl}/${scopePk}/revoke`, undefined);
	}
}
