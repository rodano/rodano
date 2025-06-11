import './extension.js';

class Bus {
	constructor() {
		this.dispatching = false;
		this.enabled = true;
		this.paused = false;
		this.locked = false;
		this.listeners = [];
		this.onEvent = undefined;

		this.awaitingEvents = [];
		this.consequenceEvents = [];
	}
	disable() {
		this.enabled = false;
	}
	enable() {
		this.enabled = true;
	}
	lock() {
		this.locked = true;
	}
	unlock() {
		this.locked = false;
	}
	reset() {
		this.listeners = [];
	}
	register(listener) {
		if(!this.locked) {
			this.listeners.push(listener);
		}
	}
	unregister(listener) {
		if(!this.locked) {
			this.listeners.removeElement(listener);
		}
	}
	isRegistered(listener) {
		return this.listeners.includes(listener);
	}
	pause() {
		this.paused = true;
	}
	resume() {
		this.paused = false;
		this.awaitingEvents.forEach(Bus.prototype.dispatch, this);
		this.awaitingEvents.length = 0;
	}
	dispatch(event) {
		//dispatch events like a wave instead of a tree
		//an event is first dispatched to all the listeners, then the consequences are dispatched
		if(!this.dispatching) {
			this.dispatching = true;
			//dispatch incoming event
			if(this.enabled) {
				if(!this.paused) {
					this.listeners.forEach(event.hit, event);
					this.onEvent?.call(undefined, event);
				}
				else {
					this.awaitingEvents.push(event);
				}
			}
			//consume consequences events
			this.dispatching = false;
			//more consequences events may pile up while the current one are being consumed
			while(this.consequenceEvents.length > 0) {
				this.dispatch(this.consequenceEvents.shift());
			}
		}
		else {
			this.consequenceEvents.push(event);
		}
	}
}

class BusEvent {
	constructor() {
	}
	//get list of callback method names for the event
	/**
	 * @returns {string[]} - A list of method names that will be called on the listener
	 * @abstract
	 */
	getCallbacks() {
		throw new Error(`getCallbacks() is not implemented for ${this.constructor.name}`);
	}
	hit(listener) {
		this.getCallbacks().forEach(c => listener[c]?.call(listener, this));
	}
}

export {Bus, BusEvent};
