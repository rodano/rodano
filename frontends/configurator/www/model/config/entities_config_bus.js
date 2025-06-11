import {BusEvent} from '../../basic-tools/bus.js';

//create node
class BusEventCreateNode extends BusEvent {
	constructor(entity, node) {
		super();
		this.entity = entity;
		this.node = node;
	}
	getCallbacks() {
		return [`onCreate${this.entity}`, 'onCreate'];
	}
}

//delete node
class BusEventDeleteNode extends BusEvent {
	constructor(entity, node) {
		super();
		this.entity = entity;
		this.node = node;
	}
	getCallbacks() {
		return [`onDelete${this.entity}`, 'onDelete'];
	}
}

//move node
class BusEventMoveNode extends BusEvent {
	constructor(entity, node, oldParent, newParent) {
		super();
		this.entity = entity;
		this.node = node;
		this.oldParent = oldParent;
		this.newParent = newParent;
	}
	getCallbacks() {
		return [`onMove${this.entity}`, 'onMove'];
	}
}

//add child
class BusEventAddChildNode extends BusEvent {
	constructor(parent, child) {
		super();
		this.parent = parent;
		this.child = child;
	}
	getCallbacks() {
		return ['onAddChild'];
	}
}

//change property
class BusEventNodeChangeProperty extends BusEvent {
	constructor(entity, node, property, oldValue, newValue) {
		super();
		this.entity = entity;
		this.node = node;
		this.property = property;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
	getCallbacks() {
		return [`onChange${this.entity}${this.property.capitalize()}`, `onChange${this.entity}`, 'onChange'];
	}
}

export {BusEventCreateNode, BusEventDeleteNode, BusEventMoveNode, BusEventAddChildNode, BusEventNodeChangeProperty};
