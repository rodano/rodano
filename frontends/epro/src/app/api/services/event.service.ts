import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EventDTO } from '../model/event-dto';
import { APIService } from './api.service';
import { WorkflowStatusDTO } from '../model/workflow-status-dto';
import { isPast, sub } from 'date-fns';
import { transformDates } from '../utilities/transform-dates';

@Injectable({
	providedIn: 'root'
})
export class EventService {

	constructor(
		private http: HttpClient,
		private apiService: APIService
	) { }

	@transformDates
	create(scopePk: number, eventModelId: string): Observable<EventDTO> {
		const params = new HttpParams().set('eventModelId', eventModelId);
		return this.http.post<EventDTO>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events`, undefined, {params});
	}

	@transformDates
	get(scopePk: number, eventPk: number): Observable<EventDTO> {
		return this.http.get<EventDTO>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}`);
	}

	@transformDates
	getForScope(scopePk: number): Observable<EventDTO[]> {
		return this.http.get<EventDTO[]>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events`);
	}

	remove(scopePk: number, eventPk: number, rationale: string) {
		const params = new HttpParams().set('rationale', rationale);
		return this.http.put(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/remove`, undefined, {params});
	}

	restore(scopePk: number, eventPk: number, rationale: string) {
		const params = new HttpParams().set('rationale', rationale);
		return this.http.put(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/restore`, undefined, {params});
	}

	lock(scopePk: number, eventPk: number) {
		return this.http.put(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/lock`, {});
	}

	unlock(scopePk: number, eventPk: number) {
		return this.http.put(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/unlock`, {});
	}

	getContainedWorkflowStatus(scopePk: number, eventPk: number) {
		return this.http.get<WorkflowStatusDTO[]>(`${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/contained-workflows`);
	}

	/**
	 * Checks if an event is planned according to the ePRO logic of event planning.
	 * @param event The event
	 * @returns True if the event is planned, false otherwise.
	 */
	isPlanned(event: EventDTO): boolean {
		return !!event.model.deadline &&
			!!event.model.deadlineUnit &&
			!!event.model.deadlineReferenceEventModelIds &&
			event.model.deadlineReferenceEventModelIds.length > 0;
	}

	/**
	 * Checks that the event is planned and is due. If the event has an interval associated with it
	 * the lower bound of the event's expected date will be taken as reference point in time,
	 * otherwise the event's exoected date acts as the reference point in time.
	 * Note that the event's true date is used in case the expected date is not present.
	 * @param event The event
	 * @returns True if the event is planned and is due, false otherwise.
	 */
	isEventPlannedAndDue(event: EventDTO): boolean {
		const eventDate = event.expectedDate ? event.expectedDate : event.date;

		if(event.model.interval && event.model.intervalUnit) {
			const intervalObject: Record<string, number> = {};
			intervalObject[event.model.intervalUnit.toLowerCase()] = event.model.interval;

			const lowerBoundDate = sub(eventDate, intervalObject);
			return this.isPlanned(event) && isPast(lowerBoundDate);
		} else {
			return this.isPlanned(event) && isPast(eventDate);
		}
	}
}
