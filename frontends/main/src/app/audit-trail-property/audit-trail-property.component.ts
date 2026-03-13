import {ChangeDetectionStrategy, Component, Inject, signal} from '@angular/core';
import {MatTableModule} from '@angular/material/table';
import {PropertyAuditTrail} from '@core/model/property-audit-trail';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {DateTimeUTCPipe} from '../pipes/date-time-utc.pipe';
import {MatButton} from '@angular/material/button';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
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
	readonly entityName = signal('');
	readonly property = signal('');
	readonly trails = signal<PropertyAuditTrail[]>([]);
	columnsToDisplay = ['value', 'by', 'date', 'context'];

	constructor(
		@Inject(MAT_DIALOG_DATA) data: {entityName: string; property: string; trails: PropertyAuditTrail[]}
	) {
		this.entityName.set(data.entityName);
		this.property.set(data.property);
		this.trails.set(data.trails);
	}
}
