import {Observable} from 'rxjs';
import {FormDTO} from '@core/model/form-dto';
import {EventDTO} from '@core/model/event-dto';

export interface CRFEventDTO extends EventDTO {
	//Used to track the forms associated with a event
	forms: Observable<FormDTO[]>;
}
