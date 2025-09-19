import {Field} from '@core/model/field';

export interface CRFField extends Field {

	//Visibility criteria status
	shown: boolean;

	//Error on the field
	error: string | undefined;
}
