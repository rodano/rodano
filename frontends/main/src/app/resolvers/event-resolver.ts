import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve} from '@angular/router';
import {Observable, EMPTY} from 'rxjs';
import {EventDTO} from '@core/model/event-dto';
import {EventService} from '@core/services/event.service';

@Injectable({
	providedIn: 'root'
})
export class EventResolver implements Resolve<EventDTO> {
	constructor(
		private eventService: EventService
	) {}

	resolve(route: ActivatedRouteSnapshot): Observable<EventDTO> {
		//Since this resolver is used on two levels of the router hierarchy, we need
		//to check both the paramMap AND the parent paramMap
		const scopePkParam = route.paramMap.get('scopePk') ?? route.parent?.paramMap.get('scopePk');
		const eventPkParam = route.paramMap.get('eventPk') ?? route.parent?.paramMap.get('eventPk');
		if(!scopePkParam || !eventPkParam) {
			return EMPTY;
		}
		const scopePk = parseInt(scopePkParam, 10);
		const eventPk = parseInt(eventPkParam, 10);
		return this.eventService.get(scopePk, eventPk);
	}
}
