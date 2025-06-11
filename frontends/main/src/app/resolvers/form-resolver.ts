import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve} from '@angular/router';
import {Observable, EMPTY} from 'rxjs';
import {FormService} from '@core/services/form.service';
import {FormDTO} from '@core/model/form-dto';

@Injectable({
	providedIn: 'root'
})
export class FormResolver implements Resolve<FormDTO> {
	constructor(
		private formService: FormService
	) {}

	resolve(route: ActivatedRouteSnapshot): Observable<FormDTO> {
		//Since this resolver is used on two levels of the router hierarchy, we need
		//to check both the paramMap AND the parent paramMap
		const scopePkParam = route.paramMap.get('scopePk') ?? route.parent?.paramMap.get('scopePk');
		const eventPkParam = route.paramMap.get('eventPk') ?? route.parent?.paramMap.get('eventPk');
		const formPkParam = route.paramMap.get('formPk') ?? route.parent?.paramMap.get('formPk');
		if(!scopePkParam || !formPkParam) {
			return EMPTY;
		}
		const scopePk = parseInt(scopePkParam, 10);
		const eventPk = eventPkParam ? parseInt(eventPkParam, 10) : undefined;
		const formPk = parseInt(formPkParam, 10);
		return eventPk
			? this.formService.getForEvent(scopePk, eventPk, formPk)
			: this.formService.getForScope(scopePk, formPk);
	}
}
