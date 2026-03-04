import {Directive, EventEmitter, Input, Output} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {LanguageService} from '../services/language.service';
import {BaseManagerService} from '../services/manager/base-manager.service';
import {BaseDetailComponent} from './base-detail.component';

@Directive()
export abstract class BaseManagerDetailComponent<
	T extends Record<string, any>,
	M extends BaseManagerService<T>
> extends BaseDetailComponent {
	@Input() entity!: T;
	@Input() allEntities: T[] = [];
	@Output() entityUpdated = new EventEmitter<T>();
	@Output() entityDeleted = new EventEmitter<string>();

	protected constructor(
		protected manager: M,
		languageService: LanguageService,
		snackBar: MatSnackBar
	) {
		super(languageService, snackBar);
	}

	protected applyUpdate(updated: T): void {
		this.manager.update(updated);
		this.entityUpdated.emit(updated);
		this.showStagedMessage();
	}

	isFieldModified(fieldName: string): boolean {
		return this.manager.isFieldModified(this.getEntityId(), fieldName);
	}

	protected abstract getEntityId(): string;
}
