import {Component} from '@angular/core';
import {MatDialogModule} from '@angular/material/dialog';
import {MatButton} from '@angular/material/button';

export interface WorkflowRationaleData {
	title: string;
	rationale: string;
}

@Component({
	selector: 'app-unsaved-changes',
	templateUrl: 'unsaved-changes.component.html',
	imports: [
		MatDialogModule,
		MatButton
	]
})
export class UnsavedChangesComponent {
}
