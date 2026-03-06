import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {BaseManagerService} from './base-manager.service';
import {FormModel} from '@core/model/form-model';
import {FormModelService} from '../api/form-model.service';

@Injectable({providedIn: 'root'})
export class FormModelManagerService extends BaseManagerService<FormModel> {
	constructor(private formModelService: FormModelService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {return (fm: FormModel) => fm.formModelId;}
	protected getSimpleFields(): (keyof FormModel)[] {
		return ['id', 'optional'];
	}

	protected getTranslationFields(): (keyof FormModel)[] {
		return ['shortname', 'longname', 'description', 'printButtonLabel'];
	}

	protected getArrayFields(): (keyof FormModel)[] {
		return ['workflowIds'];
	}

	protected fetchAll(projectId: string): Observable<FormModel[]> {
		return this.formModelService.getFormModels(projectId);
	}

	protected createEntity(projectId: string, entity: FormModel): Observable<FormModel> {
		return this.formModelService.createFormModel(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.formModelService.deleteFormModel(projectId, id);
	}
}
