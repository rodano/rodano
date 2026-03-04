import {Component, Input, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {EventGroup} from '@core/model/event-group';
import {ScopeModel} from '@core/model/scope-model';
import {EventGroupDialogService} from '../../../services/dialogs/event-group-dialog.service';
import {LanguageService} from '../../../services/language.service';
import {ConfirmationDialogComponent} from '../../../../confirmation-dialog/confirmation-dialog.component';
import {DangerZoneComponent} from '../../../shared/danger-zone/danger-zone.component';
import {BaseDraftDetailComponent} from '../../../shared/base-draft-detail.component';
import {SettingItemComponent} from '../../../shared/setting-item/setting-item.component';

@Component({
	selector: 'app-event-group-detail',
	standalone: true,
	templateUrl: './event-group-detail.component.html',
	styleUrls: ['../../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent, SettingItemComponent]
})
export class EventGroupDetailComponent extends BaseDraftDetailComponent<EventGroup> {
	@Input() eventGroupId: string | null = null;
	@Input() eventGroups: EventGroup[] = [];
	@Input() originalEventGroups: EventGroup[] = [];
	@Input() scopeModel: ScopeModel | null = null;

	@Output() eventGroupUpdated = this.entityUpdated;
	@Output() eventGroupDeleted = this.entityDeleted;

	availableLanguages: {code: string; name: string; isDefault: boolean}[] = [];
	private modifiedFields = new Set<string>();

	constructor(
		languageService: LanguageService,
		private eventGroupDialogService: EventGroupDialogService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(languageService, snackBar);
	}

	protected entityIdInputName(): string {return 'eventGroupId';}
	protected entitiesInputName(): string {return 'eventGroups';}
	protected getEntityId(): string {return this.eventGroupId || '';}
	protected getEntities(): EventGroup[] {return this.eventGroups;}
	protected getOriginals(): EventGroup[] {return this.originalEventGroups;}
	protected findInArray(arr: EventGroup[], id: string): EventGroup | undefined {
		return arr.find(eg => eg.eventGroupId === id);
	}

	protected override syncDraft(): void {
		if(!this.eventGroupId) {
			this.draftEntity = null;
			this.originalEntity = null;
			this.modifiedFields.clear();
			return;
		}
		const draft = this.eventGroups.find(eg => eg.eventGroupId === this.eventGroupId);
		const original = this.originalEventGroups.find(eg => eg.eventGroupId === this.eventGroupId);
		this.draftEntity = draft || null;
		this.originalEntity = original || null;
		this.calculateModifiedFields();
	}

	override isFieldModified(fieldName: string): boolean {
		if(this.modifiedFields.has(fieldName)) {
			return true;
		}
		const pattern = new RegExp(`^${fieldName}\\.`);
		return Array.from(this.modifiedFields).some(f => pattern.test(f));
	}

	override ngOnChanges(changes: SimpleChanges): void {
		super.ngOnChanges(changes);
		if(changes['project']) {
			this.loadLanguages();
		}
	}

	protected override onInit(): void {
		this.loadLanguages();
	}

	get draftEventGroup(): EventGroup | null {return this.draftEntity;}

	private loadLanguages(): void {
		if(this.project?.languages) {
			this.availableLanguages = this.project.languages.map(lang => ({
				code: lang.languageCode || '',
				name: this.languageService.getLanguageName(lang.languageCode),
				isDefault: lang.isDefault || false
			}));
		}
	}

	private calculateModifiedFields(): void {
		this.modifiedFields.clear();
		if(!this.draftEntity || !this.originalEntity) {
			return;
		}

		if(this.draftEntity.id !== this.originalEntity.id) {
			this.modifiedFields.add('id');
		}

		(['shortname', 'longname', 'description'] as (keyof EventGroup)[]).forEach(field => {
			const draftVal = this.draftEntity![field] as Record<string, string> | undefined;
			const origVal = this.originalEntity![field] as Record<string, string> | undefined;
			if(draftVal && origVal) {
				new Set([...Object.keys(draftVal), ...Object.keys(origVal)]).forEach(lang => {
					if(draftVal[lang] !== origVal[lang]) {
						this.modifiedFields.add(`${field as string}.${lang}`);
					}
				});
			}
			else if(draftVal !== origVal) {
				this.modifiedFields.add(field as string);
			}
		});
	}

	onEditBasicInfo(): void {
		if(!this.draftEntity) {
			return;
		}
		const scopeModelId = this.draftEntity.scopeModelId || this.scopeModel?.scopeModelId;
		if(!scopeModelId) {
			console.error('No scope model ID available');
			return;
		}
		this.eventGroupDialogService.openEditDialog(
			this.projectId, scopeModelId, this.draftEntity, this.project?.languages || []
		).subscribe((result: any) => {
			if(result) {
				this.applyUpdate(result);
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
				title: 'Delete Event Group',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.draftEntity.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe((confirmed: boolean) => {
			if(confirmed && this.draftEntity) {
				this.entityDeleted.emit(this.draftEntity.eventGroupId);
			}
		});
	}
}
