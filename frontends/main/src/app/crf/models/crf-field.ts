import {FieldDTO} from '@core/model/field-dto';

export interface CRFField extends FieldDTO {

	//Visibility criteria status
	shown: boolean;

	//Error on the field
	error: string | undefined;
}
