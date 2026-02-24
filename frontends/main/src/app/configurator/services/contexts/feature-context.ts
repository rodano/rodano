import {FeatureManagerService} from '../manager/feature-manager.service';
import {Feature} from '@core/model/feature';

export interface FeatureContext {
	featureManager: FeatureManagerService;
	features: Feature[];
	originalFeatures: Feature[];
	modifiedFeatureIds: Set<string>;
}
