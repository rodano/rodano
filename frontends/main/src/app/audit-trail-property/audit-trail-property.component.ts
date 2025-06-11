import {Component, Inject} from '@angular/core';
import {MatTableModule} from '@angular/material/table';
import {PropertyAuditTrail} from '@core/model/property-audit-trail';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {DateTimeUTCPipe} from '../pipes/date-time-utc.pipe';
import {MatButton} from '@angular/material/button';

@Component({
	selector: 'app-audit-trail-property',
	templateUrl: './audit-trail-property.component.html',
	styleUrls: ['./audit-trail-property.component.css'],
	imports: [
		MatDialogModule,
		MatTableModule,
		MatButton,
		DateTimeUTCPipe
	]
})
export class AuditTrailPropertyComponent {
	entityName: string;
	property: string;
	trails: PropertyAuditTrail[] = [];
	columnsToDisplay = ['value', 'by', 'date', 'context'];

	constructor(
		@Inject(MAT_DIALOG_DATA) data: {entityName: string; property: string; trails: PropertyAuditTrail[]}
	) {
		this.entityName = data.entityName;
		this.property = data.property;
		this.trails = data.trails;
	}
}
