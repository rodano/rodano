import {Bus, BusEvent} from './basic-tools/bus.js';

class ServerMessage extends BusEvent {
	constructor(message) {
		super();
		this.message = message;
	}

	getCallbacks() {
		switch(this.message.type) {
			case 'TASK':
				return [`onTaskMessage${this.message.task.capitalize()}`, 'onTaskMessage'];
			case 'LOG':
				return ['onLogMessage'];
		}
		return [];
	}
}

const bus = new Bus();

export {bus, ServerMessage};
