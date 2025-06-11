import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {EventDTO} from '../model/event-dto';
import {APIService} from './api.service';
import {reviveDates} from '../decorators/revive-dates.decorator';

@Injectable({
	providedIn: 'root'
})
export class EventService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) { }

	@reviveDates
	create(scopePk: number, eventModelId: string): Observable<EventDTO> {
		const params = new HttpParams().set('eventModelId', eventModelId);
		return this.http.post<EventDTO>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events`, undefined, {params});
	}

	@reviveDates
	get(scopePk: number, eventPk: number): Observable<EventDTO> {
		return this.http.get<EventDTO>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}`);
	}

	@reviveDates
	search(scopePk: number): Observable<EventDTO[]> {
		return this.http.get<EventDTO[]>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events`);
	}

	@reviveDates
	updateDates(scopePk: number, eventPk: number, date: Date, endDate?: Date): Observable<EventDTO> {
		let params = new HttpParams().set('date', date.toISOString());
		if(endDate) {
			params = params.set('endDate', endDate.toISOString());
		}
		return this.http.put<EventDTO>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}`, undefined, {params});
	}

	@reviveDates
	remove(scopePk: number, eventPk: number, message: string): Observable<EventDTO> {
		return this.http.put<EventDTO>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/remove`, {message});
	}

	@reviveDates
	restore(scopePk: number, eventPk: number, message: string): Observable<EventDTO> {
		return this.http.put<EventDTO>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/restore`, {message});
	}

	@reviveDates
	lock(scopePk: number, eventPk: number): Observable<EventDTO> {
		return this.http.put<EventDTO>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/lock`, {});
	}

	@reviveDates
	unlock(scopePk: number, eventPk: number): Observable<EventDTO> {
		return this.http.put<EventDTO>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/unlock`, {});
	}
}
