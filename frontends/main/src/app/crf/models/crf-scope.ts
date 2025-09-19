import {Observable} from 'rxjs';
import {Scope} from '@core/model/scope';
import {Form} from '@core/model/form';

export interface CRFScope extends Scope {
	//Used to track the forms on scope
	forms: Observable<Form[]>;
}
