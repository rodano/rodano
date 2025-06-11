import {Component, Inject} from '@angular/core';
import {MatTableModule} from '@angular/material/table';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {DateTimeUTCPipe} from '../pipes/date-time-utc.pipe';
import {MatButton, MatIconButton} from '@angular/material/button';
import {EntityAuditTrail} from '@core/model/entity-audit-trail';
import {MatIcon} from '@angular/material/icon';

@Component({
	selector: 'app-audit-trail-entity',
	templateUrl: './audit-trail-entity.component.html',
	styleUrls: ['./audit-trail-entity.component.css'],
	imports: [
		MatDialogModule,
		MatTableModule,
		MatButton,
		MatIcon,
		MatIconButton,
		DateTimeUTCPipe
	]
})
export class AuditTrailEntityComponent {
	entityName: string;
	trails: EntityAuditTrail[] = [];
	selectedTrail: EntityAuditTrail;

	columnsToDisplay = ['expand', 'by', 'date', 'context'];

	constructor(
		@Inject(MAT_DIALOG_DATA) data: {entityName: string; trails: EntityAuditTrail[]}
	) {
		this.entityName = data.entityName;
		this.trails = data.trails;
	}

	getModification(trail: EntityAuditTrail) {
		return Object.entries(trail.modifications).map(e => ({property: e[0], oldValue: e[1].oldValue, newValue: e[1].newValue}));
	}
}
