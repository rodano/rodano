import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Robot } from '../model/robot-dto';
import { APIService } from './api.service';

@Injectable({
	providedIn: 'root'
})
export class RobotService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) { }

	search(): Observable<Robot[]> {
		return this.http.get<Robot[]>(`${this.apiService.getApiUrl()}/robots`);
	}

	get(robotPk: number): Observable<Robot> {
		return this.http.get<Robot>(`${this.apiService.getApiUrl()}/robots/${robotPk}`);
	}

	create(robot: Robot, scopePk: number, profileId: string): Observable<Robot> {
		return this.http.post<Robot>(
			`${this.apiService.getApiUrl()}/robots`,
			robot,
			{params: {scopePk: scopePk.toString(), profileId}}
		);
	}

	save(robotPk: number, robot: Robot): Observable<Robot> {
		return this.http.put<Robot>(`${this.apiService.getApiUrl()}/robots/${robotPk}`, robot);
	}

	remove(robotPk: number) {
		return this.http.put(`${this.apiService.getApiUrl()}/robots/${robotPk}/remove`, {});
	}

	restore(robotPk: number) {
		return this.http.put(`${this.apiService.getApiUrl()}/robots/${robotPk}/restore`, {});
	}
}
