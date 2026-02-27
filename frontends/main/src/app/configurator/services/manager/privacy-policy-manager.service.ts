import {Injectable} from '@angular/core';
import {EntityModificationTracker} from '../entity-modification-tracker';
import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {PrivacyPolicy} from '@core/model/privacy-policy';
import {PrivacyPolicyService} from '../api/privacy-policy.service';

@Injectable({providedIn: 'root'})
export class PrivacyPolicyManagerService {
	private tracker: EntityModificationTracker<PrivacyPolicy>;
	private loaded = false;

	constructor(private privacyPolicyService: PrivacyPolicyService) {
		this.tracker = new EntityModificationTracker<PrivacyPolicy>(
			privacyPolicy => privacyPolicy.policyId,
			['id'],
			['shortname', 'longname', 'description', 'content'],
			['profileIds']
		);
	}

	load(projectId: string): Observable<PrivacyPolicy[]> {
		if(this.loaded) {
			return of(this.tracker.getCurrent());
		}
		return this.privacyPolicyService.getPrivacyPolicies(projectId).pipe(
			map(privacyPolicies => {
				this.tracker.initialize(privacyPolicies);
				this.loaded = true;
				return privacyPolicies;
			})
		);
	}

	invalidate(): void {
		this.loaded = false;
	}

	create(projectId: string, privacyPolicy: PrivacyPolicy): Observable<PrivacyPolicy> {
		return this.privacyPolicyService.createPrivacyPolicy(projectId, privacyPolicy).pipe(
			map(created => {
				this.tracker.addEntity(created);
				return created;
			})
		);
	}

	update(privacyPolicy: PrivacyPolicy): void {
		this.tracker.updateEntity(privacyPolicy);
	}

	delete(projectId: string, privacyPolicyId: string): Observable<void> {
		return this.privacyPolicyService.deletePrivacyPolicy(projectId, privacyPolicyId).pipe(
			map(() => {
				this.tracker.removeEntity(privacyPolicyId);
			})
		);
	}

	getModifiedIds(): Set<string> {
		return this.tracker.getModifiedIds();
	}

	getModifiedFieldsMap(): Map<string, Set<string>> {
		return this.tracker.getModifiedFieldsMap();
	}

	getOriginals(): PrivacyPolicy[] {
		return this.tracker.getOriginals();
	}

	clearModifications(): void {
		this.tracker.clearModifications();
	}

	resetToOriginals(): void {
		this.tracker.resetToOriginals();
	}

	getAll(): PrivacyPolicy[] {
		return this.tracker.getCurrent();
	}

	getById(id: string): PrivacyPolicy | undefined {
		return this.tracker.getEntity(id);
	}

	isModified(privacyPolicyId: string): boolean {
		return this.tracker.isModified(privacyPolicyId);
	}

	isFieldModified(privacyPolicyId: string, fieldName: string): boolean {
		return this.tracker.isFieldModified(privacyPolicyId, fieldName);
	}

	getModificationCount(): number {
		return this.tracker.getTotalModifiedFieldsCount();
	}

	updateOnServer(projectId: string, privacyPolicyId: string, privacyPolicy: PrivacyPolicy): Observable<PrivacyPolicy> {
		return this.privacyPolicyService.updatePrivacyPolicy(projectId, privacyPolicyId, privacyPolicy);
	}

	syncOriginalsWithCurrent(): void {
		this.tracker.syncOriginalsWithCurrent();
	}
}
