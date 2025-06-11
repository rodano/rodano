import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {LocalizeMapPipe} from '../../../pipes/localize-map.pipe';
import {MatButton} from '@angular/material/button';
import {FieldModelDTO} from '@core/model/field-model-dto';

export interface WorkflowRationaleData {
	title: string;
	rationale: string;
}

@Component({
	selector: 'app-field-model-help',
	templateUrl: 'field-model-help.component.html',
	imports: [
		MatDialogModule,
		MatButton,
		LocalizeMapPipe
	]
})
export class FieldModelHelpComponent {
	constructor(
		@Inject(MAT_DIALOG_DATA) public fieldModel: FieldModelDTO
	) {}
}
