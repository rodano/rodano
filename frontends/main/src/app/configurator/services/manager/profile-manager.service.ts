import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Profile} from '@core/model/profile';
import {ProfileService} from '../api/profile.service';
import {BaseManagerService} from './base-manager.service';

@Injectable({providedIn: 'root'})
export class ProfileManagerService extends BaseManagerService<Profile> {
	constructor(private profileService: ProfileService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {return (p: Profile) => p.profileId;}
	protected getSimpleFields(): (keyof Profile)[] {
		return ['id', 'order', 'workflowOfInterestId'];
	}

	protected getTranslationFields(): (keyof Profile)[] {
		return ['shortname', 'longname', 'description'];
	}

	protected getArrayFields(): (keyof Profile)[] {
		return [];
	}

	protected fetchAll(projectId: string): Observable<Profile[]> {
		return this.profileService.getProfiles(projectId);
	}

	protected createEntity(projectId: string, entity: Profile): Observable<Profile> {
		return this.profileService.createProfile(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.profileService.deleteProfile(projectId, id);
	}
}
