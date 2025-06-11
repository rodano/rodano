import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {RobotDTO} from '../model/robot-dto';
import {APIService} from './api.service';
import {PagedResultRobotDTO} from '../model/paged-result-robot-dto';
import {RobotSearch} from '../utilities/search/robot-search';
import {HttpParamsService} from './http-params.service';
import {RobotCreationDTO} from '../model/robot-creation-dto';
import {RobotUpdateDTO} from '../model/robot-update-dto';

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

	search(search: RobotSearch): Observable<PagedResultRobotDTO> {
		const params = this.httpParamsService.toHttpParams(search);
		return this.http.get<PagedResultRobotDTO>(this.serviceUrl, {params});
	}

	get(robotPk: number): Observable<RobotDTO> {
		return this.http.get<RobotDTO>(`${this.serviceUrl}/${robotPk}`);
	}

	create(robot: RobotCreationDTO): Observable<RobotDTO> {
		return this.http.post<RobotDTO>(this.serviceUrl, robot);
	}

	save(robotPk: number, robot: RobotUpdateDTO): Observable<RobotDTO> {
		return this.http.put<RobotDTO>(`${this.serviceUrl}/${robotPk}`, robot);
	}

	remove(robotPk: number) {
		return this.http.put(`${this.serviceUrl}/${robotPk}/remove`, {});
	}

	restore(robotPk: number) {
		return this.http.put(`${this.serviceUrl}/${robotPk}/restore`, {});
	}
}
