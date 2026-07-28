import {HttpClient, HttpParams} from '@angular/common/http';
import {Service, inject} from '@angular/core';
import {Observable} from 'rxjs';
import {Event} from '../model/event';
import {APIService} from './api.service';
import {reviveDates} from '../decorators/revive-dates.decorator';
import {isPast, sub} from 'date-fns';

@Service()
export class EventService {
	private readonly http = inject(HttpClient);
	private readonly apiService = inject(APIService);

	@reviveDates
	create(scopePk: number, eventModelId: string): Observable<Event> {
		const params = new HttpParams().set('eventModelId', eventModelId);
		return this.http.post<Event>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events`, undefined, {params});
	}

	@reviveDates
	get(scopePk: number, eventPk: number): Observable<Event> {
		return this.http.get<Event>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}`);
	}

	@reviveDates
	search(scopePk: number): Observable<Event[]> {
		return this.http.get<Event[]>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events`);
	}

	@reviveDates
	updateDates(scopePk: number, eventPk: number, date: Date, endDate?: Date): Observable<Event> {
		let params = new HttpParams().set('date', date.toISOString());
		if(endDate) {
			params = params.set('endDate', endDate.toISOString());
		}
		return this.http.put<Event>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}`, undefined, {params});
	}

	@reviveDates
	remove(scopePk: number, eventPk: number, message: string): Observable<Event> {
		return this.http.put<Event>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/remove`, {message});
	}

	@reviveDates
	restore(scopePk: number, eventPk: number, message: string): Observable<Event> {
		return this.http.put<Event>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/restore`, {message});
	}

	@reviveDates
	lock(scopePk: number, eventPk: number): Observable<Event> {
		return this.http.put<Event>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/lock`, {});
	}

	@reviveDates
	unlock(scopePk: number, eventPk: number): Observable<Event> {
		return this.http.put<Event>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/unlock`, {});
	}

	/**
	 * Checks if an event is planned according to the ePRO logic of event planning.
	 * @param event The event
	 * @returns True if the event is planned, false otherwise.
	 */
	isPlanned(event: Event): boolean {
		return !!event.model.deadline && !!event.model.deadlineUnit && !!event.model.deadlineReferenceEventModelIds && event.model.deadlineReferenceEventModelIds.length > 0;
	}

	/**
	 * Checks that the event is planned and is due. If the event has an interval associated with it
	 * the lower bound of the event's expected date will be taken as reference point in time,
	 * otherwise the event's expected date acts as the reference point in time.
	 * Note that the event's true date is used in case the expected date is not present.
	 * @param event The event
	 * @returns True if the event is planned and is due, false otherwise.
	 */
	isEventPlannedAndDue(event: Event): boolean {
		const eventDate = event.expectedDate ? event.expectedDate : event.date;

		if(event.model.interval && event.model.intervalUnit) {
			const intervalObject: Record<string, number> = {};
			intervalObject[event.model.intervalUnit.toLowerCase()] = event.model.interval;

			const lowerBoundDate = sub(eventDate, intervalObject);
			return this.isPlanned(event) && isPast(lowerBoundDate);
		}
		else {
			return this.isPlanned(event) && isPast(eventDate);
		}
	}
}
