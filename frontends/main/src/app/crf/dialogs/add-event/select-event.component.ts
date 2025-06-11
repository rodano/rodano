import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {EventModelDTO} from '@core/model/event-model-dto';
import {LocalizeMapPipe} from '../../../pipes/localize-map.pipe';
import {MatButton} from '@angular/material/button';
import {MatListModule} from '@angular/material/list';

export interface WorkflowRationaleData {
	title: string;
	rationale: string;
}

@Component({
	selector: 'app-select-event',
	templateUrl: 'select-event.component.html',
	imports: [
		MatDialogModule,
		MatListModule,
		MatButton,
		LocalizeMapPipe
	]
})
export class SelectEventComponent {
	constructor(
		@Inject(MAT_DIALOG_DATA) public events: EventModelDTO[]
	) {}
}
