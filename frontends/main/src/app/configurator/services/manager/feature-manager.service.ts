import {Injectable} from '@angular/core';
import {EntityModificationTracker} from '../entity-modification-tracker';
import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {FeatureService} from '../api/feature.service';
import {Feature} from '@core/model/feature';

@Injectable({providedIn: 'root'})
export class FeatureManagerService {
	private tracker: EntityModificationTracker<Feature>;
	private loaded = false;

	constructor(private featureService: FeatureService) {
		this.tracker = new EntityModificationTracker<Feature>(
			feature => feature.featureId,
			['id', 'optional'],
			['shortname', 'longname', 'description'],
			[]
		);
	}

	load(projectId: string): Observable<Feature[]> {
		if(this.loaded) {
			return of(this.tracker.getCurrent());
		}
		return this.featureService.getFeatures(projectId).pipe(
			map(features => {
				this.tracker.initialize(features);
				this.loaded = true;
				return features;
			})
		);
	}

	invalidate(): void {
		this.loaded = false;
	}

	create(projectId: string, feature: Feature): Observable<Feature> {
		return this.featureService.createFeature(projectId, feature).pipe(
			map(created => {
				this.tracker.addEntity(created);
				return created;
			})
		);
	}

	update(feature: Feature): void {
		this.tracker.updateEntity(feature);
	}

	delete(projectId: string, featureId: string): Observable<void> {
		return this.featureService.deleteFeature(projectId, featureId).pipe(
			map(() => {
				this.tracker.removeEntity(featureId);
			})
		);
	}

	getModifiedIds(): Set<string> {
		return this.tracker.getModifiedIds();
	}

	getModifiedFieldsMap(): Map<string, Set<string>> {
		return this.tracker.getModifiedFieldsMap();
	}

	getOriginals(): Feature[] {
		return this.tracker.getOriginals();
	}

	clearModifications(): void {
		this.tracker.clearModifications();
	}

	resetToOriginals(): void {
		this.tracker.resetToOriginals();
	}

	getAll(): Feature[] {
		return this.tracker.getCurrent();
	}

	getById(id: string): Feature | undefined {
		return this.tracker.getEntity(id);
	}

	isModified(featureId: string): boolean {
		return this.tracker.isModified(featureId);
	}

	isFieldModified(featureId: string, fieldName: string): boolean {
		return this.tracker.isFieldModified(featureId, fieldName);
	}

	getModificationCount(): number {
		return this.tracker.getTotalModifiedFieldsCount();
	}

	updateOnServer(projectId: string, featureId: string, feature: Feature): Observable<Feature> {
		return this.featureService.updateFeature(projectId, featureId, feature);
	}

	syncOriginalsWithCurrent(): void {
		this.tracker.syncOriginalsWithCurrent();
	}
}
