import {Observable} from 'rxjs';
import {ScopeDTO} from '@core/model/scope-dto';
import {FormDTO} from '@core/model/form-dto';

export interface CRFScopeDTO extends ScopeDTO {
	//Used to track the forms on scope
	forms: Observable<FormDTO[]>;
}
