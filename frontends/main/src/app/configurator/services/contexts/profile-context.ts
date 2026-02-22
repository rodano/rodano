import {ProfileManagerService} from '../manager/profile-manager.service';
import {Profile} from '@core/model/profile';

export interface ProfileContext {
	profileManager: ProfileManagerService;
	profiles: Profile[];
	originalProfiles: Profile[];
	modifiedProfileIds: Set<string>;
}
