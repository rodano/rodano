import {WritableSignal} from '@angular/core';
import {Field} from '@core/model/field';

export interface CRFField extends Field {

	//Visibility criteria status
	shown: boolean;

	//Error on the field
	error: WritableSignal<string | undefined>;
}
