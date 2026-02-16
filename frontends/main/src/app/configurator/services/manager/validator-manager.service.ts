import {Injectable} from '@angular/core';
import {Validator} from '@core/model/validator';
import {EntityModificationTracker} from '../entity-modification-tracker';
import {ValidatorService} from '../api/validator.service';
import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';

@Injectable({providedIn: 'root'})
export class ValidatorManagerService {
	private tracker: EntityModificationTracker<Validator>;
	private loaded = false;

	constructor(private validatorService: ValidatorService) {
		this.tracker = new EntityModificationTracker<Validator>(
			validator => validator.validatorId,
			['id', 'required', 'script', 'workflowId', 'invalidStateId', 'validStateId'],
			['shortname', 'longname', 'description', 'message'],
			[]
		);
	}

	load(projectId: string): Observable<Validator[]> {
		if(this.loaded) {
			return of(this.tracker.getCurrent());
		}
		return this.validatorService.getValidators(projectId).pipe(
			map(validators => {
				this.tracker.initialize(validators);
				this.loaded = true;
				return validators;
			})
		);
	}

	invalidate(): void {
		this.loaded = false;
	}

	create(projectId: string, validator: Validator): Observable<Validator> {
		return this.validatorService.createValidator(projectId, validator).pipe(
			map(created => {
				this.tracker.addEntity(created);
				return created;
			})
		);
	}

	update(validator: Validator): void {
		this.tracker.updateEntity(validator);
	}

	delete(projectId: string, validatorId: string): Observable<void> {
		return this.validatorService.deleteValidator(projectId, validatorId).pipe(
			map(() => {
				this.tracker.removeEntity(validatorId);
			})
		);
	}

	getModifiedIds(): Set<string> {
		return this.tracker.getModifiedIds();
	}

	getModifiedFieldsMap(): Map<string, Set<string>> {
		return this.tracker.getModifiedFieldsMap();
	}

	getOriginals(): Validator[] {
		return this.tracker.getOriginals();
	}

	clearModifications(): void {
		this.tracker.clearModifications();
	}

	resetToOriginals(): void {
		this.tracker.resetToOriginals();
	}

	getAll(): Validator[] {
		return this.tracker.getCurrent();
	}

	getById(id: string): Validator | undefined {
		return this.tracker.getEntity(id);
	}

	isModified(validatorId: string): boolean {
		return this.tracker.isModified(validatorId);
	}

	isFieldModified(validatorId: string, fieldName: string): boolean {
		return this.tracker.isFieldModified(validatorId, fieldName);
	}

	getModificationCount(): number {
		return this.tracker.getTotalModifiedFieldsCount();
	}

	updateOnServer(projectId: string, validatorId: string, validator: Validator): Observable<Validator> {
		return this.validatorService.updateValidator(projectId, validatorId, validator);
	}

	syncOriginalsWithCurrent(): void {
		this.tracker.syncOriginalsWithCurrent();
	}
}
