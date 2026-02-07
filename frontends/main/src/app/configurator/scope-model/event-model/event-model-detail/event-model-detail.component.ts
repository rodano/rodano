import {Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges, OnDestroy} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {EventModel} from '@core/model/event-model';
import {ScopeModel} from '@core/model/scope-model';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {MatDialog} from '@angular/material/dialog';
import {Subscription} from 'rxjs';
import {
	EventModelBasicInfoDialogComponent
} from '../../scope-model-dialog/event-model-basic-info-dialog/event-model-basic-info-dialog.component';
import {LanguageService} from '../../../services/language.service';
import {
	EventModelSchedulingDialogComponent
} from '../../scope-model-dialog/event-model-scheduling-dialog/event-model-scheduling-dialog.component';
import {
	EventModelLabelPatternDialogComponent
} from '../../scope-model-dialog/event-model-label-pattern-dialog/event-model-label-pattern-dialog.component';
import {
	EventModelResourcesDialogComponent
} from '../../scope-model-dialog/event-model-resources-dialog/event-model-resources-dialog.component';
import {
	EventModelRelationshipsDialogComponent
} from '../../scope-model-dialog/event-model-relationships-dialog/event-model-relationships-dialog.component';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../../confirmation-dialog/confirmation-dialog.component';
import {EventGroup} from '@core/model/event-group';

@Component({
	selector: 'app-event-model-detail',
	standalone: true,
	templateUrl: './event-model-detail.component.html',
	styleUrls: ['../../event-detail-shared.css', './event-model-detail.component.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule]
})
export class EventModelDetailComponent implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() eventModelId = '';
	@Input() eventModels: EventModel[] = [];
	@Input() eventGroups: EventGroup[] = [];
	@Input() originalEventModels: EventModel[] = [];
	@Input() scopeModel: ScopeModel | null = null;
	@Input() project: ConfiguratorProject | null = null;
	@Output() _close = new EventEmitter<void>();
	@Output() eventModelUpdated = new EventEmitter<any>();
	@Output() eventModelDeleted = new EventEmitter<string>();

	originalEventModel: EventModel | null = null;
	draftEventModel: EventModel | null = null;
	selectedLanguage = '';
	private languageSubscription: Subscription;

	constructor(
		private languageService: LanguageService,
		private dialog: MatDialog,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
		this.loadEventModel();
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['eventModelId'] || changes['eventModels']) {
			this.loadEventModel();
		}
	}

	ngOnDestroy(): void {
		if(this.languageSubscription) {
			this.languageSubscription.unsubscribe();
		}
	}

	private loadEventModel(): void {
		const eventModel = this.eventModels.find(em => em.eventModelId === this.eventModelId);
		this.draftEventModel = eventModel ? JSON.parse(JSON.stringify(eventModel)) : null;

		const original = this.originalEventModels.find(em => em.eventModelId === this.eventModelId);
		this.originalEventModel = original ? JSON.parse(JSON.stringify(original)) : null;
	}

	isFieldModified(field: keyof EventModel): boolean {
		if(!this.originalEventModel || !this.draftEventModel) {
			return false;
		}
		return JSON.stringify(this.originalEventModel[field]) !== JSON.stringify(this.draftEventModel[field]);
	}

	onClose(): void {
		this._close.emit();
	}

	getTranslatedValue(translations: Record<string, string> | undefined): string {
		if(!translations) {
			return '';
		}
		return translations[this.selectedLanguage] || '';
	}

	getLanguageName(code: string | undefined): string {
		if(!code) {
			return 'Unknown';
		}
		try {
			const displayNames = new Intl.DisplayNames(['en'], {type: 'language'});
			return displayNames.of(code) || code.toUpperCase();
		}
		catch (error) {
			console.error(error);
			return code.toUpperCase();
		}
	}

	getEventModelName(eventModelId: string): string {
		const em = this.eventModels.find(e => e.eventModelId === eventModelId);
		return em ? (em.shortname?.['en'] || em.shortname?.['de'] || em.id) : eventModelId;
	}

	getEventModelCode(eventModelId: string): string {
		const eventModel = this.eventModels.find(em => em.eventModelId === eventModelId);
		if(eventModel) {
			const shortname = eventModel.shortname?.['en'] || eventModel.id;
			return `${shortname} (${eventModel.id})`;
		}
		return eventModelId;
	}

	getEventGroupName(eventGroupId: string | undefined): string {
		if(!eventGroupId) {
			return 'None';
		}
		const eventGroup = this.eventGroups.find(eg => eg.eventGroupId === eventGroupId);
		return eventGroup ? (eventGroup.shortname?.['en'] || eventGroup.shortname?.['de'] || eventGroup.id) : eventGroupId;
	}

	onEditBasicInfo(): void {
		const formattedEventGroups = this.eventGroups.map(eg => ({
			id: eg.eventGroupId,
			name: eg.shortname?.['en'] || eg.shortname?.['de'] || eg.id
		}));

		const dialogRef = this.dialog.open(EventModelBasicInfoDialogComponent, {
			data: {
				eventModel: this.draftEventModel,
				languages: this.project?.languages || [],
				eventGroups: formattedEventGroups
			},
			width: '500px',
			disableClose: true
		});

		dialogRef.afterClosed().subscribe(result => {
			if(result && this.draftEventModel) {
				this.draftEventModel = {
					...this.draftEventModel,
					...result
				};
				this.eventModelUpdated.emit(this.draftEventModel);
			}
		});
	}

	onEditScheduling(): void {
		const dialogRef = this.dialog.open(EventModelSchedulingDialogComponent, {
			data: {
				eventModel: this.draftEventModel,
				allEventModels: this.eventModels
			},
			width: '500px',
			disableClose: true
		});

		dialogRef.afterClosed().subscribe(result => {
			if(result && this.draftEventModel) {
				this.draftEventModel = {
					...this.draftEventModel,
					...result
				};
				this.eventModelUpdated.emit(this.draftEventModel);
			}
		});
	}

	onEditLabelPattern(): void {
		const dialogRef = this.dialog.open(EventModelLabelPatternDialogComponent, {
			data: {
				eventModel: this.draftEventModel
			},
			width: '500px',
			disableClose: true
		});

		dialogRef.afterClosed().subscribe(result => {
			if(result && this.draftEventModel) {
				this.draftEventModel = {
					...this.draftEventModel,
					...result
				};
				this.eventModelUpdated.emit(this.draftEventModel);
			}
		});
	}

	onEditResources(): void {
		const dialogRef = this.dialog.open(EventModelResourcesDialogComponent, {
			data: {
				eventModel: this.draftEventModel,
				availableFormModels: [],
				availableDatasetModels: [],
				availableWorkflows: []
			},
			width: '500px',
			maxHeight: '90vh',
			disableClose: true
		});

		dialogRef.afterClosed().subscribe(result => {
			if(result && this.draftEventModel) {
				this.draftEventModel = {
					...this.draftEventModel,
					...result
				};
				this.eventModelUpdated.emit(this.draftEventModel);
			}
		});
	}

	onEditRelationships(): void {
		const dialogRef = this.dialog.open(EventModelRelationshipsDialogComponent, {
			width: '500px',
			maxHeight: '90vh',
			data: {
				eventModel: JSON.parse(JSON.stringify(this.draftEventModel)),
				availableEventModels: this.eventModels
			},
			disableClose: true
		});

		dialogRef.afterClosed().subscribe(result => {
			if(result && this.draftEventModel) {
				this.draftEventModel = {
					...this.draftEventModel,
					...result
				};
				this.eventModelUpdated.emit(this.draftEventModel);
			}
		});
	}

	onDelete(): void {
		if(!this.draftEventModel) {
			return;
		}

		const eventModelName = this.getTranslatedValue(this.draftEventModel.shortname) || this.draftEventModel.id;

		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Event Model',
				message: `Are you sure you want to delete "${eventModelName}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});

		dialogRef.afterClosed().subscribe((confirmed: boolean) => {
			if(confirmed && this.draftEventModel) {
				this.eventModelDeleted.emit(this.draftEventModel.eventModelId);
			}
		});
	}
}
