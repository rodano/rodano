import {Component, Input, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {LanguageService} from '../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {TimelineGraph} from '@core/model/timeline-graph';
import {TimelineGraphSection} from '@core/model/timeline-graph-section';
import {TimelineGraphSectionManagerService} from '../../services/manager/timeline-graph-section-manager.service';
import {BaseDraftDetailComponent} from '../../shared/base-draft-detail.component';
import {TimelineGraphSectionDialogService} from '../../services/dialogs/timeline-graph-section-dialog.service';
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';
import {SettingItemComponent} from '../../shared/setting-item/setting-item.component';
import {EventModelManagerService} from '../../services/manager/event-model-manager.service';
import {DatasetModelManagerService} from '../../services/manager/dataset-model-manager.service';
import {FieldModelManagerService} from '../../services/manager/field-model-manager.service';
import {UsedByComponent} from '../../shared/used-by/used-by.component';
import {ConfiguratorNavigationService} from '../../services/configurator-navigation.service';

@Component({
	selector: 'app-timeline-graph-section-detail',
	standalone: true,
	templateUrl: './timeline-graph-section-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent, SettingItemComponent,
		UsedByComponent]
})
export class TimelineGraphSectionDetailComponent extends BaseDraftDetailComponent<TimelineGraphSection> {
	@Input() graphSectionId = '';
	@Input() sections: TimelineGraphSection[] = [];
	@Input() originalSections: TimelineGraphSection[] = [];
	@Input() timelineGraph: TimelineGraph | null = null;

	@Output() timelineGraphSectionUpdated = this.entityUpdated;
	@Output() timelineGraphSectionDeleted = this.entityDeleted;

	constructor(
		languageService: LanguageService,
		public navigationService: ConfiguratorNavigationService,
		private timelineGraphSectionDialogService: TimelineGraphSectionDialogService,
		private timelineGraphSectionManager: TimelineGraphSectionManagerService,
		private eventModelManager: EventModelManagerService,
		private datasetModelManager: DatasetModelManagerService,
		private fieldModelManager: FieldModelManagerService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(languageService, snackBar);
	}

	protected entityIdInputName(): string {return 'graphSectionId';}
	protected entitiesInputName(): string {return 'sections';}
	protected getEntityId(): string {return this.graphSectionId;}
	protected getEntities(): TimelineGraphSection[] {return this.sections;}
	protected getOriginals(): TimelineGraphSection[] {return this.originalSections;}
	protected findInArray(arr: TimelineGraphSection[], id: string): TimelineGraphSection | undefined {
		return arr.find(tgs => tgs.graphSectionId === id);
	}

	override ngOnChanges(changes: SimpleChanges): void {
		if(changes['graphSectionId'] && this.graphSectionId) {
			this.timelineGraphSectionManager.load(this.projectId).subscribe({
				next: () => this.syncDraft(),
				error: error => console.error('Error loading sections:', error)
			});
		}
		else if(changes['sections']) {
			this.syncDraft();
		}
	}

	get draftSection(): TimelineGraphSection | null {return this.draftEntity;}

	onEditBasicInfo(): void {
		if(!this.draftEntity || !this.timelineGraph) {
			return;
		}

		this.timelineGraphSectionDialogService.openBasicInfoDialog(
			this.draftEntity, this.projectId,
			this.timelineGraph.timelineGraphId, this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onEditResources(): void {
		if(!this.draftEntity) {
			return;
		}

		this.timelineGraphSectionDialogService.openResourceDialog(
			this.projectId, this.draftEntity
		).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onEditAppearance(): void {
		if(!this.draftEntity) {
			return;
		}

		this.timelineGraphSectionDialogService.openAppearanceDialog(
			this.projectId, this.draftEntity, this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onEditScale(): void {
		if(!this.draftEntity) {
			return;
		}

		this.timelineGraphSectionDialogService.openScaleDialog(
			this.projectId, this.draftEntity
		).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onEditPosition(): void {
		if(!this.draftEntity) {
			return;
		}

		this.timelineGraphSectionDialogService.openPositionDialog(
			this.projectId, this.draftEntity
		).subscribe(result => {
			if(result) {
				this.applyUpdate(result);
			}
		});
	}

	onEditReference(): void {
		if(!this.draftEntity) {
			return;
		}

		this.timelineGraphSectionDialogService.openReferenceDialog(
			this.projectId, this.draftEntity, this.sections, this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate({references: result});
			}
		});
	}

	onDelete(): void {
		if(!this.draftEntity) {
			return;
		}
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Timeline Graph Section',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.draftEntity.label)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed && this.draftEntity) {
				this.entityDeleted.emit(this.draftEntity.graphSectionId);
			}
		});
	}

	get showScale(): boolean {
		return ['LINE', 'DOT', 'BAR'].includes(this.draftSection?.type ?? '');
	}

	get showPosition(): boolean {
		return ['LINE', 'DOT', 'BAR', 'PERIOD', 'DATE'].includes(this.draftSection?.type ?? '');
	}

	getEventModelLabel(eventModelId: string): string {
		return this.languageService.getLabelById(eventModelId, id => this.eventModelManager.getById(id));
	}

	getDatasetModelLabel(datasetModelId: string): string {
		return this.languageService.getLabelById(datasetModelId, id => this.datasetModelManager.getById(id));
	}

	getFieldModelLabel(fieldModelId: string): string {
		return this.languageService.getLabelById(fieldModelId, id => this.fieldModelManager.getById(id));
	}

	getSectionLabel(sectionId: string): string {
		const section = this.sections.find(s => s.graphSectionId === sectionId);
		return section ? (this.languageService.getTranslatedName(section.label) || section.id) : sectionId;
	}
}
