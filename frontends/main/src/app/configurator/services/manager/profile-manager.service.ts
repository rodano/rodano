import {Injectable} from '@angular/core';
import {EntityModificationTracker} from '../entity-modification-tracker';
import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {Profile} from '@core/model/profile';
import {ProfileService} from '../api/profile.service';

@Injectable({providedIn: 'root'})
export class ProfileManagerService {
	private tracker: EntityModificationTracker<Profile>;
	private loaded = false;

	constructor(private profileService: ProfileService) {
		this.tracker = new EntityModificationTracker<Profile>(
			profile => profile.profileId,
			['id', 'order', 'workflowOfInterestId'],
			['shortname', 'longname', 'description'],
			[]
		);
	}

	load(projectId: string): Observable<Profile[]> {
		if(this.loaded) {
			return of(this.tracker.getCurrent());
		}
		return this.profileService.getProfiles(projectId).pipe(
			map(profiles => {
				this.tracker.initialize(profiles);
				this.loaded = true;
				return profiles;
			})
		);
	}

	invalidate(): void {
		this.loaded = false;
	}

	create(projectId: string, profile: Profile): Observable<Profile> {
		return this.profileService.createProfile(projectId, profile).pipe(
			map(created => {
				this.tracker.addEntity(created);
				return created;
			})
		);
	}

	update(profile: Profile): void {
		this.tracker.updateEntity(profile);
	}

	delete(projectId: string, profileId: string): Observable<void> {
		return this.profileService.deleteProfile(projectId, profileId).pipe(
			map(() => {
				this.tracker.removeEntity(profileId);
			})
		);
	}

	getModifiedIds(): Set<string> {
		return this.tracker.getModifiedIds();
	}

	getModifiedFieldsMap(): Map<string, Set<string>> {
		return this.tracker.getModifiedFieldsMap();
	}

	getOriginals(): Profile[] {
		return this.tracker.getOriginals();
	}

	clearModifications(): void {
		this.tracker.clearModifications();
	}

	resetToOriginals(): void {
		this.tracker.resetToOriginals();
	}

	getAll(): Profile[] {
		return this.tracker.getCurrent();
	}

	getById(id: string): Profile | undefined {
		return this.tracker.getEntity(id);
	}

	isModified(profileId: string): boolean {
		return this.tracker.isModified(profileId);
	}

	isFieldModified(profileId: string, fieldName: string): boolean {
		return this.tracker.isFieldModified(profileId, fieldName);
	}

	getModificationCount(): number {
		return this.tracker.getTotalModifiedFieldsCount();
	}

	updateOnServer(projectId: string, profileId: string, profile: Profile): Observable<Profile> {
		return this.profileService.updateProfile(projectId, profileId, profile);
	}

	syncOriginalsWithCurrent(): void {
		this.tracker.syncOriginalsWithCurrent();
	}
}
