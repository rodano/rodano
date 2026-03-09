import {FormModelManagerService} from '../manager/form-model-manager.service';
import {FormModel} from '@core/model/form-model';
import {FormLayoutManagerService} from '../manager/form-layout-manager.service';
import {Layout} from '@core/model/layout';

export interface FormModelContext {
	formModelManager: FormModelManagerService;
	formLayoutManager: FormLayoutManagerService;
	formModels: FormModel[];
	originalFormModels: FormModel[];
	modifiedFormModelIds: Set<string>;
	layouts: Layout[];
	originalLayouts: Layout[];
	modifiedLayoutIds: Set<string>;
	formModelId: string;
}
