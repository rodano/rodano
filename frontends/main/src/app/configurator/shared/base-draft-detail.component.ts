import {Directive, EventEmitter, OnChanges, Output, SimpleChanges} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {LanguageService} from '../services/language.service';
import {BaseDetailComponent} from './base-detail.component';

@Directive()
export abstract class BaseDraftDetailComponent<T extends Record<string, any>>
	extends BaseDetailComponent
	implements OnChanges {
	@Output() entityUpdated = new EventEmitter<T>();
	@Output() entityDeleted = new EventEmitter<string>();

	draftEntity: T | null = null;
	originalEntity: T | null = null;

	protected constructor(
		languageService: LanguageService,
		snackBar: MatSnackBar
	) {
		super(languageService, snackBar);
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes[this.entityIdInputName()] || changes[this.entitiesInputName()]) {
			this.syncDraft();
		}
	}

	protected abstract entityIdInputName(): string;

	protected abstract entitiesInputName(): string;

	protected abstract getEntityId(): string;

	protected abstract findInArray(arr: T[], id: string): T | undefined;

	protected abstract getEntities(): T[];

	protected abstract getOriginals(): T[];

	protected syncDraft(): void {
		const id = this.getEntityId();
		if(!id) {
			this.draftEntity = null;
			this.originalEntity = null;
			return;
		}

		const entity = this.findInArray(this.getEntities(), id);
		this.draftEntity = entity ? JSON.parse(JSON.stringify(entity)) : null;

		const original = this.findInArray(this.getOriginals(), id);
		this.originalEntity = original ? JSON.parse(JSON.stringify(original)) : null;
	}

	protected applyUpdate(result: Partial<T>): void {
		if(!this.draftEntity) {
			return;
		}
		this.draftEntity = {...this.draftEntity, ...result};
		this.entityUpdated.emit(this.draftEntity);
	}

	isFieldModified(field: keyof T): boolean {
		if(!this.originalEntity || !this.draftEntity) {
			return false;
		}
		return JSON.stringify(this.originalEntity[field]) !== JSON.stringify(this.draftEntity[field]);
	}
}
