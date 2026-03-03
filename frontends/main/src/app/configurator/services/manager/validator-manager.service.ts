import {Injectable} from '@angular/core';
import {Validator} from '@core/model/validator';
import {ValidatorService} from '../api/validator.service';
import {Observable} from 'rxjs';
import {BaseManagerService} from './base-manager.service';

@Injectable({providedIn: 'root'})
export class ValidatorManagerService extends BaseManagerService<Validator> {
	constructor(private validatorService: ValidatorService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {return (v: Validator) => v.validatorId;}
	protected getSimpleFields(): (keyof Validator)[] {
		return ['id', 'required', 'script', 'workflowId', 'invalidStateId', 'validStateId'];
	}

	protected getTranslationFields(): (keyof Validator)[] {
		return ['shortname', 'longname', 'description', 'message'];
	}

	protected getArrayFields(): (keyof Validator)[] {
		return [];
	}

	protected fetchAll(projectId: string): Observable<Validator[]> {
		return this.validatorService.getValidators(projectId);
	}

	protected createEntity(projectId: string, entity: Validator): Observable<Validator> {
		return this.validatorService.createValidator(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.validatorService.deleteValidator(projectId, id);
	}
}
