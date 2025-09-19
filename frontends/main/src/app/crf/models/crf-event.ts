import {Observable} from 'rxjs';
import {Form} from '@core/model/form';
import {Event} from '@core/model/event';

export interface CRFEvent extends Event {
	//Used to track the forms associated with a event
	forms: Observable<Form[]>;
}
