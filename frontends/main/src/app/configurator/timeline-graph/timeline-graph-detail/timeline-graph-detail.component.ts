import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {LanguageService} from '../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {BaseManagerDetailComponent} from '../../shared/base-manager-detail.component';
import {TimelineGraph} from '@core/model/timeline-graph';
import {TimelineGraphManagerService} from '../../services/manager/timeline-graph-manager.service';
import {TimelineGraphDialogService} from '../../services/dialogs/timeline-graph-dialog.service';
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';
import {SettingItemComponent} from '../../shared/setting-item/setting-item.component';
import {ScopeModelManagerService} from '../../services/manager/scope-model-manager.service';
import {EventModelManagerService} from '../../services/manager/event-model-manager.service';
import {UsedByComponent} from '../../shared/used-by/used-by.component';
import {ConfiguratorNavigationService} from '../../services/configurator-navigation.service';

@Component({
	selector: 'app-timeline-graph-detail',
	standalone: true,
	templateUrl: './timeline-graph-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent, SettingItemComponent,
		UsedByComponent]
})
export class TimelineGraphDetailComponent extends BaseManagerDetailComponent<TimelineGraph, TimelineGraphManagerService> {
	@Input() override entity!: TimelineGraph;
	@Input() override allEntities: TimelineGraph[] = [];

	@Input() set timelineGraph(v: TimelineGraph) {this.entity = v;}
	get timelineGraph(): TimelineGraph {return this.entity;}

	@Input() set allTimelineGraphs(v: TimelineGraph[]) {this.allEntities = v;}

	@Output() timelineGraphUpdated = this.entityUpdated;
	@Output() timelineGraphDeleted = this.entityDeleted;

	@Output() switchToSections = new EventEmitter<void>();

	constructor(
		timelineGraphManager: TimelineGraphManagerService,
		languageService: LanguageService,
		public navigationService: ConfiguratorNavigationService,
		private scopeModelManager: ScopeModelManagerService,
		private eventModelManager: EventModelManagerService,
		private timelineGraphDialogService: TimelineGraphDialogService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(timelineGraphManager, languageService, snackBar);
	}

	protected getEntityId(): string {return this.entity.timelineGraphId;}

	onEditBasicInfo(): void {
		this.timelineGraphDialogService.openBasicInfoDialog(
			this.projectId,
			this.entity,
			this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onEditPeriod(): void {
		this.timelineGraphDialogService.openPeriodDialog(this.entity).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onEditDesign(): void {
		this.timelineGraphDialogService.openDesignDialog(this.entity).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onDelete(): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Timeline Graph',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.entity.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.entityDeleted.emit(this.entity.timelineGraphId);
			}
		});
	}

	onSwitchToSections(): void {this.switchToSections.emit();}

	getScopeModelLabel(scopeModelId: string): string {
		return this.languageService.getLabelById(scopeModelId, id => this.scopeModelManager.getById(id));
	}

	getEventModelLabel(eventModelId: string): string {
		return this.languageService.getLabelById(eventModelId, id => this.eventModelManager.getById(id));
	}
}
