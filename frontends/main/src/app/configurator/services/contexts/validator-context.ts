import {ValidatorManagerService} from '../manager/validator-manager.service';
import {Validator} from '@core/model/validator';

export interface ValidatorContext {
	validatorManager: ValidatorManagerService;
	validators: Validator[];
	originalValidators: Validator[];
	modifiedValidatorIds: Set<string>;
}
