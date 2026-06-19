import {Component, Inject, OnInit, signal} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {LocalizeMapPipe} from '../pipes/localize-map.pipe';
import {MatButton} from '@angular/material/button';
import {MatProgressBar} from '@angular/material/progress-bar';
import {Field} from '@core/model/field';
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
		MatProgressBar,
		MatTableModule,
		LocalizeMapPipe,
		DateTimeUTCPipe
	]
})
export class AuditTrailFieldComponent implements OnInit {
	readonly events = signal<FieldEventAuditTrail[]>([]);
	readonly selectedEntityPk = signal<number | undefined>(undefined);
	readonly loading = signal(false);
	columnsToDisplay = ['event', 'value', 'by', 'date', 'context'];

	constructor(
		private auditTrailService: AuditTrailService,
		@Inject(MAT_DIALOG_DATA) public field: Field
	) {
	}

	ngOnInit(): void {
		this.loading.set(true);
		this.auditTrailService.getForField(this.field).subscribe(events => {
			this.events.set(events);
			this.loading.set(false);
		});
	}

	highlightRelatedEvents(event: FieldEventAuditTrail) {
		if(event.entityType === FieldEventEntityType.WORKFLOW_STATUS) {
			this.selectedEntityPk.set(event.entityPk);
		}
		else {
			this.selectedEntityPk.set(undefined);
		}
	}
}
