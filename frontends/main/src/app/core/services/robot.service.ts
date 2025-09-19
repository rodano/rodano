import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Robot} from '../model/robot';
import {APIService} from './api.service';
import {PagedResultRobot} from '../model/paged-result-robot';
import {RobotSearch} from '../utilities/search/robot-search';
import {HttpParamsService} from './http-params.service';
import {RobotCreation} from '../model/robot-creation';
import {RobotUpdate} from '../model/robot-update';

@Injectable({
	providedIn: 'root'
})
export class RobotService {
	private serviceUrl: string;

	constructor(
		private http: HttpClient,
		private httpParamsService: HttpParamsService,
		private apiService: APIService
	) {
		this.serviceUrl = `${this.apiService.getApiUrl()}/robots`;
	}

	search(search: RobotSearch): Observable<PagedResultRobot> {
		const params = this.httpParamsService.toHttpParams(search);
		return this.http.get<PagedResultRobot>(this.serviceUrl, {params});
	}

	get(robotPk: number): Observable<Robot> {
		return this.http.get<Robot>(`${this.serviceUrl}/${robotPk}`);
	}

	create(robot: RobotCreation): Observable<Robot> {
		return this.http.post<Robot>(this.serviceUrl, robot);
	}

	save(robotPk: number, robot: RobotUpdate): Observable<Robot> {
		return this.http.put<Robot>(`${this.serviceUrl}/${robotPk}`, robot);
	}

	remove(robotPk: number) {
		return this.http.put(`${this.serviceUrl}/${robotPk}/remove`, {});
	}

	restore(robotPk: number) {
		return this.http.put(`${this.serviceUrl}/${robotPk}/restore`, {});
	}
}
