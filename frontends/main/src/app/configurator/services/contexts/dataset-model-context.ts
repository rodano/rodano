import {DatasetModelManagerService} from '../manager/dataset-model-manager.service';
import {FieldModelManagerService} from '../manager/field-model-manager.service';
import {DatasetModel} from '@core/model/dataset-model';
import {FieldModel} from '@core/model/field-model';

export interface DatasetModelContext {
	datasetModelManager: DatasetModelManagerService;
	fieldModelManager: FieldModelManagerService;
	datasetModels: DatasetModel[];
	fieldModels: FieldModel[];
	originalDatasetModels: DatasetModel[];
	modifiedDatasetModelIds: Set<string>;
	modifiedFieldModels: Set<string>;
}
