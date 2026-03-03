import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {EntityModificationTracker} from '../entity-modification-tracker';

export abstract class BaseManagerService<T extends Record<string, any>> {
	protected tracker!: EntityModificationTracker<T>;
	protected loaded = false;

	protected abstract getIdFn(): (entity: T) => string;
	protected abstract getSimpleFields(): (keyof T)[];
	protected abstract getTranslationFields(): (keyof T)[];
	protected abstract getArrayFields(): (keyof T)[];
	protected abstract fetchAll(projectId: string): Observable<T[]>;
	protected abstract createEntity(projectId: string, entity: T): Observable<T>;
	protected abstract deleteEntity(projectId: string, id: string): Observable<void>;

	protected initTracker(): void {
		this.tracker = new EntityModificationTracker<T>(
			this.getIdFn(),
			this.getSimpleFields(),
			this.getTranslationFields(),
			this.getArrayFields()
		);
	}

	load(projectId: string): Observable<T[]> {
		if(this.loaded) {
			return of(this.tracker.getCurrent());
		}
		return this.fetchAll(projectId).pipe(
			map(entities => {
				this.tracker.initialize(entities);
				this.loaded = true;
				return this.tracker.getCurrent();
			})
		);
	}

	invalidate(): void {
		this.loaded = false;
	}

	isLoaded(): boolean {
		return this.loaded;
	}

	create(projectId: string, entity: T): Observable<T> {
		return this.createEntity(projectId, entity).pipe(
			map(created => {
				this.tracker.addEntity(created);
				return created;
			})
		);
	}

	update(entity: T): void {
		this.tracker.updateEntity(entity);
	}

	delete(projectId: string, id: string): Observable<void> {
		return this.deleteEntity(projectId, id).pipe(
			map(() => this.tracker.removeEntity(id))
		);
	}

	getAll(): T[] {
		return this.tracker.getCurrent();
	}

	getById(id: string): T | undefined {
		return this.tracker.getEntity(id);
	}

	getOriginals(): T[] {
		return this.tracker.getOriginals();
	}

	getModifiedIds(): Set<string> {
		return this.tracker.getModifiedIds();
	}

	getModifiedFieldsMap(): Map<string, Set<string>> {
		return this.tracker.getModifiedFieldsMap();
	}

	getModificationCount(): number {
		return this.tracker.getModifiedIds().size;
	}

	isModified(id: string): boolean {
		return this.tracker.isModified(id);
	}

	isFieldModified(id: string, fieldName: string): boolean {
		return this.tracker.isFieldModified(id, fieldName);
	}

	clearModifications(): void {
		this.tracker.clearModifications();
	}

	resetToOriginals(): void {
		this.tracker.resetToOriginals();
	}

	syncOriginalsWithCurrent(): void {
		this.tracker.syncOriginalsWithCurrent();
	}
}
