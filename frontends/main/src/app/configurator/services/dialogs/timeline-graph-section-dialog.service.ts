import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {
	TimelineGraphSectionBasicInfoDialogComponent,
	TimelineGraphSectionBasicInfoDialogData
} from '../../dialogs/timeline-graph-section/timeline-graph-section-basic-info-dialog/timeline-graph-section-basic-info-dialog.component';
import {TimelineGraphSection} from '@core/model/timeline-graph-section';
import {
	TimelineGraphSectionResourcesDialogComponent,
	TimelineGraphSectionResourcesDialogData
} from '../../dialogs/timeline-graph-section/timeline-graph-section-resource-dialog/timeline-graph-section-resource-dialog.component';
import {EventModelManagerService} from '../manager/event-model-manager.service';
import {DatasetModelManagerService} from '../manager/dataset-model-manager.service';
import {FieldModelManagerService} from '../manager/field-model-manager.service';
import {
	TimelineGraphSectionAppearanceDialogComponent, TimelineGraphSectionAppearanceDialogData
} from '../../dialogs/timeline-graph-section/timeline-graph-section-appearance-dialog/timeline-graph-section-appearance-dialog.component';
import {
	TimelineGraphSectionScaleDialogComponent, TimelineGraphSectionScaleDialogData
} from '../../dialogs/timeline-graph-section/timeline-graph-section-scale-dialog/timeline-graph-section-scale-dialog.component';
import {
	TimelineGraphSectionPositionDialogComponent, TimelineGraphSectionPositionDialogData
} from '../../dialogs/timeline-graph-section/timeline-graph-section-position-dialog/timeline-graph-section-position-dialog.component';
import {
	TimelineGraphSectionReferencesDialogComponent, TimelineGraphSectionReferencesDialogData
} from '../../dialogs/timeline-graph-section/timeline-graph-section-references-dialog/timeline-graph-section-references-dialog.component';

@Injectable({
	providedIn: 'root'
})
export class TimelineGraphSectionDialogService {
	constructor(
		private eventModelManager: EventModelManagerService,
		private datasetModelManager: DatasetModelManagerService,
		private fieldModelManager: FieldModelManagerService,
		private dialog: MatDialog
	) {}

	openCreateDialog(
		projectId: string,
		timelineGraphId: string,
		languages: any[]
	): Observable<any> {
		const dialogRef = this.dialog.open(TimelineGraphSectionBasicInfoDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				projectId,
				timelineGraphId,
				section: null,
				languages
			} as TimelineGraphSectionBasicInfoDialogData
		});

		return dialogRef.afterClosed();
	}

	openBasicInfoDialog(
		section: TimelineGraphSection,
		projectId: string,
		timelineGraphId: string,
		languages: any[]
	): Observable<any> {
		const dialogRef = this.dialog.open(TimelineGraphSectionBasicInfoDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				projectId,
				timelineGraphId,
				section,
				languages
			} as TimelineGraphSectionBasicInfoDialogData
		});

		return dialogRef.afterClosed();
	}

	openResourceDialog(projectId: string, section: TimelineGraphSection): Observable<any> {
		return this.dialog.open(TimelineGraphSectionResourcesDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				section: JSON.parse(JSON.stringify(section)),
				eventModels: this.eventModelManager.getAll(),
				fieldModels: this.fieldModelManager.getAll(),
				datasetModels: this.datasetModelManager.getAll()
			} as TimelineGraphSectionResourcesDialogData
		}).afterClosed();
	}

	openAppearanceDialog(projectId: string, section: TimelineGraphSection, languages: any[]): Observable<any> {
		return this.dialog.open(TimelineGraphSectionAppearanceDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				section,
				languages
			} as TimelineGraphSectionAppearanceDialogData
		}).afterClosed();
	}

	openScaleDialog(projectId: string, section: TimelineGraphSection): Observable<any> {
		return this.dialog.open(TimelineGraphSectionScaleDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				section
			} as TimelineGraphSectionScaleDialogData
		}).afterClosed();
	}

	openPositionDialog(projectId: string, section: TimelineGraphSection): Observable<any> {
		return this.dialog.open(TimelineGraphSectionPositionDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				section
			} as TimelineGraphSectionPositionDialogData
		}).afterClosed();
	}

	openReferenceDialog(projectId: string,
		section: TimelineGraphSection,
		sections: TimelineGraphSection[],
		languages: any[]): Observable<any> {
		return this.dialog.open(TimelineGraphSectionReferencesDialogComponent, {
			width: '500px',
			disableClose: true,
			data: {
				section,
				allSections: sections,
				languages
			} as TimelineGraphSectionReferencesDialogData
		}).afterClosed();
	}
}
