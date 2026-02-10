import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {EventModel} from '@core/model/event-model';
import {ScopeModel} from '@core/model/scope-model';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {MatDialog} from '@angular/material/dialog';
import {Subscription} from 'rxjs';
import {LanguageService} from '../../../services/language.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../../confirmation-dialog/confirmation-dialog.component';
import {EventGroup} from '@core/model/event-group';
import {EventModelDialogService} from '../../../services/dialogs/event-model-dialog.service';

@Component({
	selector: 'app-event-model-detail',
	standalone: true,
	templateUrl: './event-model-detail.component.html',
	styleUrls: ['./event-model-detail.component.css'],
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatTooltipModule]
})
export class EventModelDetailComponent implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() eventModelId = '';
	@Input() eventModels: EventModel[] = [];
	@Input() eventGroups: EventGroup[] = [];
	@Input() originalEventModels: EventModel[] = [];
	@Input() scopeModel: ScopeModel | null = null;
	@Input() project: ConfiguratorProject | null = null;
	@Output() closed = new EventEmitter<void>();
	@Output() eventModelUpdated = new EventEmitter<any>();
	@Output() eventModelDeleted = new EventEmitter<string>();

	originalEventModel: EventModel | null = null;
	draftEventModel: EventModel | null = null;
	selectedLanguage = '';
	private languageSubscription: Subscription;

	constructor(
		private languageService: LanguageService,
		private eventModelDialogService: EventModelDialogService,
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
		this.closed.emit();
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
		if(!em) {
			return eventModelId;
		}

		const name = this.languageService.getDefaultTranslation(em.shortname) || em.id;
		return `${name} (${em.id})`;
	}

	getEventModelCode(eventModelId: string): string {
		const eventModel = this.eventModels.find(em => em.eventModelId === eventModelId);
		if(!eventModel) {
			return eventModelId;
		}

		const shortname = this.languageService.getDefaultTranslation(eventModel.shortname) || eventModel.id;
		return `${shortname} (${eventModel.id})`;
	}

	getEventGroupName(eventGroupId: string | undefined): string {
		if(!eventGroupId) {
			return 'None';
		}
		const eventGroup = this.eventGroups.find(eg => eg.eventGroupId === eventGroupId);
		if(!eventGroup) {
			return eventGroupId;
		}

		const name = this.languageService.getDefaultTranslation(eventGroup.shortname) || eventGroup.id;
		return `${name} (${eventGroup.id})`;
	}

	onEditBasicInfo(): void {
		if(!this.draftEventModel || !this.scopeModel) {
			return;
		}

		const formattedEventGroups = this.eventGroups.map(eg => ({
			id: eg.eventGroupId,
			name: this.languageService.getDefaultTranslation(eg.shortname) || eg.id,
			code: eg.id
		}));

		this.eventModelDialogService.openBasicInfoDialog(
			this.draftEventModel,
			this.projectId,
			this.scopeModel.scopeModelId,
			this.project?.languages || [],
			formattedEventGroups
		).subscribe(result => {
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
		if(!this.draftEventModel) {
			return;
		}

		this.eventModelDialogService.openSchedulingDialog(
			this.draftEventModel,
			this.eventModels
		).subscribe(result => {
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
		if(!this.draftEventModel) {
			return;
		}

		this.eventModelDialogService.openLabelPatternDialog(
			this.draftEventModel
		).subscribe(result => {
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
		if(!this.draftEventModel) {
			return;
		}

		this.eventModelDialogService.openResourcesDialog(
			this.draftEventModel
		).subscribe(result => {
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
		if(!this.draftEventModel) {
			return;
		}

		this.eventModelDialogService.openRelationshipsDialog(
			this.draftEventModel,
			this.eventModels
		).subscribe(result => {
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
