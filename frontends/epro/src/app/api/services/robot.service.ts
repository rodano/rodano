import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RobotDTO } from '../model/robot-dto';
import { APIService } from './api.service';

@Injectable({
	providedIn: 'root'
})
export class RobotService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) { }

	search(): Observable<RobotDTO[]> {
		return this.http.get<RobotDTO[]>(`${this.apiService.getApiUrl()}/robots`);
	}

	get(robotPk: number): Observable<RobotDTO> {
		return this.http.get<RobotDTO>(`${this.apiService.getApiUrl()}/robots/${robotPk}`);
	}

	create(robot: RobotDTO, scopePk: number, profileId: string): Observable<RobotDTO> {
		return this.http.post<RobotDTO>(
			`${this.apiService.getApiUrl()}/robots`,
			robot,
			{params: {scopePk: scopePk.toString(), profileId}}
		);
	}

	save(robotPk: number, robot: RobotDTO): Observable<RobotDTO> {
		return this.http.put<RobotDTO>(`${this.apiService.getApiUrl()}/robots/${robotPk}`, robot);
	}

	remove(robotPk: number) {
		return this.http.put(`${this.apiService.getApiUrl()}/robots/${robotPk}/remove`, {});
	}

	restore(robotPk: number) {
		return this.http.put(`${this.apiService.getApiUrl()}/robots/${robotPk}/restore`, {});
	}
}
