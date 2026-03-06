import {FormModelManagerService} from '../manager/form-model-manager.service';
import {FormModel} from '@core/model/form-model';

export interface FormModelContext {
	formModelManager: FormModelManagerService;
	formModels: FormModel[];
	originalFormModels: FormModel[];
	modifiedFormModelIds: Set<string>;
}
