import {ScopeModelManagerService} from '../manager/scope-model-manager.service';
import {EventModelManagerService} from '../manager/event-model-manager.service';
import {EventGroupManagerService} from '../manager/event-group-manager.service';
import {ScopeModel} from '@core/model/scope-model';
import {EventModel} from '@core/model/event-model';
import {EventGroup} from '@core/model/event-group';

export interface ScopeModelContext {
	scopeModelManager: ScopeModelManagerService;
	eventModelManager: EventModelManagerService;
	eventGroupManager: EventGroupManagerService;
	scopeModels: ScopeModel[];
	eventModels: EventModel[];
	eventGroups: EventGroup[];
	originalScopeModels: ScopeModel[];
	modifiedScopeModelIds: Set<string>;
	modifiedEventModelIds: Set<string>;
	modifiedEventGroupIds: Set<string>;
}
