import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {EventGroup} from '@core/model/event-group';
import {ScopeModel} from '@core/model/scope-model';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {Subscription} from 'rxjs';
import {LanguageService} from '../../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmationDialogComponent} from '../../../../confirmation-dialog/confirmation-dialog.component';
import {EventGroupDialogService} from '../../../services/dialogs/event-group-dialog.service';
import {DangerZoneComponent} from '../../../shared/danger-zone/danger-zone.component';

@Component({
	selector: 'app-event-group-detail',
	standalone: true,
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatTooltipModule,
		DangerZoneComponent
	],
	templateUrl: './event-group-detail.component.html',
	styleUrls: ['../../../shared/detail-shared.css']
})
export class EventGroupDetailComponent implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() eventGroupId: string | null = null;
	@Input() eventGroups: EventGroup[] = [];
	@Input() originalEventGroups: EventGroup[] = [];
	@Input() scopeModel: ScopeModel | null = null;
	@Input() project: ConfiguratorProject | null = null;

	@Output() closed = new EventEmitter<void>();
	@Output() eventGroupUpdated = new EventEmitter<EventGroup>();
	@Output() eventGroupDeleted = new EventEmitter<string>();

	draftEventGroup: EventGroup | null = null;
	originalEventGroup: EventGroup | null = null;
	modifiedFields = new Set<string>();

	selectedLanguage = '';
	private languageSubscription: Subscription;

	availableLanguages: {code: string; name: string; isDefault: boolean}[] = [];

	constructor(
		public languageService: LanguageService,
		private eventGroupDialogService: EventGroupDialogService,
		private dialog: MatDialog
	) {
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnInit(): void {
		this.loadLanguages();
		this.loadEventGroup();
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['eventGroupId'] || changes['eventGroups']) {
			this.loadEventGroup();
		}

		if(changes['project']) {
			this.loadLanguages();
		}
	}

	ngOnDestroy(): void {
		this.languageSubscription?.unsubscribe();
	}

	private loadLanguages(): void {
		if(this.project?.languages) {
			this.availableLanguages = this.project.languages.map(lang => ({
				code: lang.languageCode || '',
				name: this.languageService.getLanguageName(lang.languageCode),
				isDefault: lang.isDefault || false
			}));
		}
	}

	private loadEventGroup(): void {
		if(!this.eventGroupId) {
			this.draftEventGroup = null;
			this.originalEventGroup = null;
			return;
		}

		const draft = this.eventGroups.find(eg => eg.eventGroupId === this.eventGroupId);
		const original = this.originalEventGroups.find(eg => eg.eventGroupId === this.eventGroupId);

		if(draft) {
			this.draftEventGroup = draft;
			this.originalEventGroup = original || null;
			this.calculateModifiedFields();
		}
	}

	private calculateModifiedFields(): void {
		this.modifiedFields.clear();

		if(!this.draftEventGroup || !this.originalEventGroup) {
			return;
		}

		const draft = this.draftEventGroup;
		const original = this.originalEventGroup;

		if(draft.id !== original.id) {
			this.modifiedFields.add('id');
		}

		const translationFields: (keyof EventGroup)[] = ['shortname', 'longname', 'description'];

		translationFields.forEach(field => {
			const draftValue = draft[field] as Record<string, string> | undefined;
			const originalValue = original[field] as Record<string, string> | undefined;

			if(draftValue && originalValue) {
				const allLanguages = new Set([
					...Object.keys(draftValue),
					...Object.keys(originalValue)
				]);

				allLanguages.forEach(lang => {
					if(draftValue[lang] !== originalValue[lang]) {
						this.modifiedFields.add(`${field as string}.${lang}`);
					}
				});
			}
			else if(draftValue !== originalValue) {
				this.modifiedFields.add(field as string);
			}
		});
	}

	isFieldModified(fieldName: string): boolean {
		if(this.modifiedFields.has(fieldName)) {
			return true;
		}

		const languageFieldPattern = new RegExp(`^${fieldName}\\.`);
		return Array.from(this.modifiedFields).some(field => languageFieldPattern.test(field));
	}

	onClose(): void {
		this.closed.emit();
	}

	onEditBasicInfo(): void {
		if(!this.draftEventGroup) {
			return;
		}

		const scopeModelId = this.draftEventGroup.scopeModelId || this.scopeModel?.scopeModelId;

		if(!scopeModelId) {
			console.error('No scope model ID available');
			return;
		}

		this.eventGroupDialogService.openEditDialog(
			this.projectId,
			scopeModelId,
			this.draftEventGroup,
			this.project?.languages || []
		).subscribe((result: any) => {
			if(result && this.draftEventGroup) {
				const updatedEventGroup: EventGroup = {
					...this.draftEventGroup,
					...result
				};

				this.eventGroupUpdated.emit(updatedEventGroup);
			}
		});
	}

	onDelete(): void {
		if(!this.draftEventGroup) {
			return;
		}

		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Event Group',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.draftEventGroup.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});

		dialogRef.afterClosed().subscribe((confirmed: boolean) => {
			if(confirmed && this.draftEventGroup) {
				this.eventGroupDeleted.emit(this.draftEventGroup.eventGroupId);
			}
		});
	}
}
