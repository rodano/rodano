import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {EventGroupDialogComponent, EventGroupDialogData} from '../../dialogs/event-group/event-group-dialog/event-group-dialog.component';
import {EventGroup} from '@core/model/event-group';

@Injectable({
	providedIn: 'root'
})
export class EventGroupDialogService {
	constructor(
		private dialog: MatDialog
	) {}

	openCreateDialog(
		projectId: string,
		scopeModelId: string,
		languages: any[]
	): Observable<any> {
		const dialogRef = this.dialog.open(EventGroupDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				projectId,
				scopeModelId,
				eventGroup: null,
				languages
			} as EventGroupDialogData
		});

		return dialogRef.afterClosed();
	}

	openEditDialog(
		projectId: string,
		scopeModelId: string,
		eventGroup: EventGroup,
		languages: any[]
	): Observable<any> {
		const dialogRef = this.dialog.open(EventGroupDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				projectId,
				scopeModelId,
				eventGroup: JSON.parse(JSON.stringify(eventGroup)),
				languages
			} as EventGroupDialogData
		});

		return dialogRef.afterClosed();
	}
}
