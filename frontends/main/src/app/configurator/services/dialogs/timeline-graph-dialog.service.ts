import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {Injectable} from '@angular/core';
import {ProjectLanguage} from '@core/model/project-language';
import {Observable} from 'rxjs';
import {
	TimelineGraphBasicInfoDialogComponent,
	TimelineGraphBasicInfoDialogData
} from '../../dialogs/timeline-graph/timeline-graph-basic-info-dialog/timeline-graph-basic-info-dialog.component';
import {TimelineGraph} from '@core/model/timeline-graph';
import {ScopeModelManagerService} from '../manager/scope-model-manager.service';
import {
	TimelineGraphPeriodDialogComponent
} from '../../dialogs/timeline-graph/timeline-graph-period-dialog/timeline-graph-period-dialog.component';
import {EventModelManagerService} from '../manager/event-model-manager.service';
import {
	TimelineGraphDesignDialogComponent
} from '../../dialogs/timeline-graph/timeline-graph-design-dialog/timeline-graph-design-dialog.component';

@Injectable({
	providedIn: 'root'
})
export class TimelineGraphDialogService {
	constructor(
		private scopeModelManager: ScopeModelManagerService,
		private eventModelManager: EventModelManagerService,
		private dialog: MatDialog
	) {}

	openCreateDialog(
		projectId: string,
		languages: ProjectLanguage[]
	): Observable<any> {
		const dialogRef: MatDialogRef<TimelineGraphBasicInfoDialogComponent> = this.dialog.open(
			TimelineGraphBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {
					projectId,
					timelineGraph: null,
					languages,
					scopeModels: this.scopeModelManager.getAll()
				} as TimelineGraphBasicInfoDialogData
			}
		);

		return dialogRef.afterClosed();
	}

	openBasicInfoDialog(
		projectId: string,
		timelineGraph: TimelineGraph,
		languages: ProjectLanguage[]
	): Observable<any> {
		const clonedTimelineGraph = JSON.parse(JSON.stringify(timelineGraph));

		const dialogRef: MatDialogRef<TimelineGraphBasicInfoDialogComponent> = this.dialog.open(
			TimelineGraphBasicInfoDialogComponent,
			{
				width: '500px',
				disableClose: true,
				data: {
					projectId,
					timelineGraph: clonedTimelineGraph,
					languages,
					scopeModels: this.scopeModelManager.getAll()
				} as TimelineGraphBasicInfoDialogData
			}
		);

		return dialogRef.afterClosed();
	}

	openPeriodDialog(timelineGraph: TimelineGraph): Observable<any> {
		const dialogRef = this.dialog.open(TimelineGraphPeriodDialogComponent, {
			width: '500px',
			data: {
				timelineGraph: JSON.parse(JSON.stringify(timelineGraph)),
				eventModels: this.eventModelManager.getAll()
			}
		});

		return dialogRef.afterClosed();
	}

	openDesignDialog(timelineGraph: TimelineGraph): Observable<any> {
		const dialogRef = this.dialog.open(TimelineGraphDesignDialogComponent, {
			width: '500px',
			data: {
				timelineGraph: JSON.parse(JSON.stringify(timelineGraph))
			}
		});

		return dialogRef.afterClosed();
	}
}
