import {PrivacyPolicyManagerService} from '../manager/privacy-policy-manager.service';
import {PrivacyPolicy} from '@core/model/privacy-policy';

export interface PrivacyPolicyContext {
	privacyPolicyManager: PrivacyPolicyManagerService;
	privacyPolicies: PrivacyPolicy[];
	originalPrivacyPolicies: PrivacyPolicy[];
	modifiedPrivacyPolicyIds: Set<string>;
}
