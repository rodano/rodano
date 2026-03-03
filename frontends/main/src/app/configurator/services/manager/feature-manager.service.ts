import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {FeatureService} from '../api/feature.service';
import {Feature} from '@core/model/feature';
import {BaseManagerService} from './base-manager.service';

@Injectable({providedIn: 'root'})
export class FeatureManagerService extends BaseManagerService<Feature> {
	constructor(private featureService: FeatureService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {return (f: Feature) => f.featureId;}
	protected getSimpleFields(): (keyof Feature)[] {
		return ['id', 'optional'];
	}

	protected getTranslationFields(): (keyof Feature)[] {
		return ['shortname', 'longname', 'description'];
	}

	protected getArrayFields(): (keyof Feature)[] {
		return [];
	}

	protected fetchAll(projectId: string): Observable<Feature[]> {
		return this.featureService.getFeatures(projectId);
	}

	protected createEntity(projectId: string, entity: Feature): Observable<Feature> {
		return this.featureService.createFeature(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.featureService.deleteFeature(projectId, id);
	}
}
