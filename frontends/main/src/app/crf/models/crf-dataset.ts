import {Dataset} from '@core/model/dataset';
import {CRFField} from './crf-field';

export interface CRFDataset extends Dataset {

	fields: CRFField[];

	show: boolean;

	expanded: boolean;

	rationale: string | undefined;
}
