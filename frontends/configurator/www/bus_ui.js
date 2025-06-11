import {Bus, BusEvent} from './basic-tools/bus.js';
import {bus} from './model/config/entities_hooks.js';

class BusEventUpdateSetting extends BusEvent {
	constructor(setting, value) {
		super();
		this.setting = setting;
		this.value = value;
	}
	getCallbacks() {
		return ['onUpdateSetting'];
	}
}

class BusEventLoadStudy extends BusEvent {
	constructor(study, settings) {
		super();
		this.study = study;
		this.settings = settings;
	}
	getCallbacks() {
		return ['onLoadStudy'];
	}
}

class BusEventUnloadStudy extends BusEvent {
	constructor(study, settings) {
		super();
		this.study = study;
		this.settings = settings;
	}
	getCallbacks() {
		return ['onUnloadStudy'];
	}
}

class BusEventSaveNode extends BusEvent {
	constructor(entity, node) {
		super();
		this.entity = entity;
		this.node = node;
	}
	getCallbacks() {
		return [`onSave${this.entity}`, 'onSave'];
	}
}

const bus_ui = new Bus();
//dispatch events from the bus used for the model to bus used for ui
bus.onEvent = function(event) {
	//delay bus ui events to let time for all bus model events to happen
	//for example, if the id of a scope model is updated, some events may be updated (those linked to the scope model via the property scopeModelId)
	//in this case, the representation of the events must not be updated while the whole model has been updated
	//otherwise, as soon as the scope model id is updated, the representations of linked events will be updated (see the onChange method of node links)
	//this will fail because the scopeModelId property has not been updated yet and the representation of the event relies on this property (in the isUsed method)
	//setTimeout(() => bus_ui.dispatch(event), 0);
	//unfortunately this breaks the tests that don't wait for the UI to be refreshed
	//TODO find a better way to delay the event
	//current solution is to dispatch events as a wave instead of a tree (see Bus.dispatch method)
	bus_ui.dispatch(event);
};

export {bus_ui, BusEventUpdateSetting, BusEventLoadStudy, BusEventUnloadStudy, BusEventSaveNode};
