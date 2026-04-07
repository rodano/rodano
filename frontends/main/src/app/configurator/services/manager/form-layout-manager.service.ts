import {Injectable} from '@angular/core';
import {forkJoin, Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {Layout} from '@core/model/layout';
import {FormLayoutService} from '../api/form-layout.service';
import {EntityModificationTracker} from '../entity-modification-tracker';

@Injectable({providedIn: 'root'})
export class FormLayoutManagerService {
	private tracker!: EntityModificationTracker<Layout>;
	private loaded = false;
	private currentFormModelId: string | null = null;

	constructor(private formLayoutService: FormLayoutService) {
		this.initTracker();
	}

	private initTracker(): void {
		this.tracker = new EntityModificationTracker<Layout>(
			(l: Layout) => l.formLayoutId,
			['id', 'type', 'sortOrder', 'datasetModel', 'defaultSortFieldModelId', 'cssCode', 'formModelId'],
			['description', 'textBefore', 'textAfter'],
			['lines', 'columns']
		);
	}

	load(projectId: string, formModelId: string): Observable<Layout[]> {
		if(this.loaded && this.currentFormModelId === formModelId) {
			return of(this.tracker.getCurrent());
		}
		return this.formLayoutService.getLayouts(projectId, formModelId).pipe(
			map(layouts => {
				this.tracker.initialize(layouts);
				this.loaded = true;
				this.currentFormModelId = formModelId;
				return this.tracker.getCurrent();
			})
		);
	}

	loadAllForFormModels(projectId: string, formModelIds: string[]): Observable<Layout[]> {
		if(formModelIds.length === 0) {
			return of([]);
		}
		return forkJoin(
			formModelIds.map(id =>
				this.formLayoutService.getLayouts(projectId, id)
			)
		).pipe(
			map(results => {
				const all = results.flat();
				this.tracker.initialize(all);
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

	getCurrentFormModelId(): string | null {
		return this.currentFormModelId;
	}

	getAll(): Layout[] {
		return this.tracker.getCurrent();
	}

	getById(id: string): Layout | undefined {
		return this.tracker.getEntity(id);
	}

	getOriginals(): Layout[] {
		return this.tracker.getOriginals();
	}

	getModifiedIds(): Set<string> {
		return this.tracker.getModifiedIds();
	}

	getModificationCount(): number {
		return this.tracker.getModifiedIds().size;
	}

	isModified(id: string): boolean {
		return this.tracker.isModified(id);
	}

	update(layout: Layout): void {
		this.tracker.updateEntity(layout);
	}

	addLayout(layout: Layout): void {
		this.tracker.addEntity(layout);
	}

	removeLayout(id: string): void {
		this.tracker.removeEntity(id);
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

	reinitializeFromServer(savedLayouts: Layout[]): void {
		const current = this.tracker.getCurrent();
		const updated = current.map(existing => {
			const fromServer = savedLayouts.find(s => s.formLayoutId === existing.formLayoutId);
			return fromServer ?? existing;
		});
		this.tracker.initialize(updated);
		this.loaded = true;
	}
}
