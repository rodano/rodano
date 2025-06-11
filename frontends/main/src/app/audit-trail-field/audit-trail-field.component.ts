import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {LocalizeMapPipe} from '../pipes/localize-map.pipe';
import {MatButton} from '@angular/material/button';
import {FieldDTO} from '@core/model/field-dto';
import {DateTimeUTCPipe} from '../pipes/date-time-utc.pipe';
import {MatTableModule} from '@angular/material/table';
import {AuditTrailService} from '@core/services/audit-trail.service';
import {FieldEventAuditTrail, FieldEventEntityType} from '@core/model/field-event-audit-trail';

export interface WorkflowRationaleData {
	title: string;
	rationale: string;
}

@Component({
	selector: 'app-audit-trail-field',
	templateUrl: 'audit-trail-field.component.html',
	styleUrls: ['./audit-trail-field.component.css'],
	imports: [
		MatDialogModule,
		MatButton,
		MatTableModule,
		LocalizeMapPipe,
		DateTimeUTCPipe
	]
})
export class AuditTrailFieldComponent implements OnInit {
	events: FieldEventAuditTrail[];
	selectedEntityPk: number | undefined;
	columnsToDisplay = ['event', 'value', 'by', 'date', 'context'];

	constructor(
		private auditTrailService: AuditTrailService,
		@Inject(MAT_DIALOG_DATA) public field: FieldDTO
	) {
	}

	ngOnInit(): void {
		this.auditTrailService.getForField(this.field).subscribe(e => this.events = e);
	}

	highlightRelatedEvents(event: FieldEventAuditTrail) {
		if(event.entityType === FieldEventEntityType.WORKFLOW_STATUS) {
			this.selectedEntityPk = event.entityPk;
		}
		else {
			this.selectedEntityPk = undefined;
		}
	}
}
