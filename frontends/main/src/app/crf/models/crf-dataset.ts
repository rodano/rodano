import {DatasetDTO} from '@core/model/dataset-dto';
import {CRFField} from './crf-field';

export interface CRFDataset extends DatasetDTO {

	fields: CRFField[];

	show: boolean;

	expanded: boolean;

	rationale: string | undefined;
}
