import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {PrivacyPolicy} from '@core/model/privacy-policy';
import {PrivacyPolicyService} from '../api/privacy-policy.service';
import {BaseManagerService} from './base-manager.service';

@Injectable({providedIn: 'root'})
export class PrivacyPolicyManagerService extends BaseManagerService<PrivacyPolicy> {
	constructor(private privacyPolicyService: PrivacyPolicyService) {
		super();
		this.initTracker();
	}

	protected getIdFn() {return (pp: PrivacyPolicy) => pp.policyId;}
	protected getSimpleFields(): (keyof PrivacyPolicy)[] {
		return ['id'];
	}

	protected getTranslationFields(): (keyof PrivacyPolicy)[] {
		return ['shortname', 'longname', 'description', 'content'];
	}

	protected getArrayFields(): (keyof PrivacyPolicy)[] {
		return ['profileIds'];
	}

	protected fetchAll(projectId: string): Observable<PrivacyPolicy[]> {
		return this.privacyPolicyService.getPrivacyPolicies(projectId);
	}

	protected createEntity(projectId: string, entity: PrivacyPolicy): Observable<PrivacyPolicy> {
		return this.privacyPolicyService.createPrivacyPolicy(projectId, entity);
	}

	protected deleteEntity(projectId: string, id: string): Observable<void> {
		return this.privacyPolicyService.deletePrivacyPolicy(projectId, id);
	}
}
