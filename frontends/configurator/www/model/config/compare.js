const DifferenceTypes = {
	ADDITION: 'Addition',
	DELETION: 'Deletion',
	MODIFICATION: 'Modification'
};

class DifferenceArrayLength {
	constructor(node, property, way, element) {
		this.node = node;
		this.property = property;
		//way, true for a new child and false for a missing child
		this.way = way;
		this.element = element;
	}
	getType() {
		return this.way ? DifferenceTypes.ADDITION : DifferenceTypes.DELETION;
	}
}

class DifferenceArrayOrdering {
	constructor(node, property) {
		this.node = node;
		this.property = property;
	}
	getType() {
		return DifferenceTypes.MODIFICATION;
	}
}

class DifferenceArrayElement {
	constructor(node, property, index, element, other_element) {
		this.node = node;
		this.property = property;
		this.index = index;
		this.element = element;
		this.otherElement = other_element;
	}
	getType() {
		return DifferenceTypes.MODIFICATION;
	}
}

class DifferenceProperty {
	constructor(node, property, value, other_value) {
		this.node = node;
		this.property = property;
		this.value = value;
		this.otherValue = other_value;
	}
	getType() {
		return DifferenceTypes.MODIFICATION;
	}
}

class DifferenceChild {
	constructor(node, child_entity, child_id, way) {
		this.node = node;
		this.childEntity = child_entity;
		this.childId = child_id;
		//way, true for a new child and false for a missing child
		this.way = way;
	}
	getType() {
		return this.way ? DifferenceTypes.ADDITION : DifferenceTypes.DELETION;
	}
	isStructural() {
		return this.childEntity.comparison_structural;
	}
}

export {DifferenceArrayLength, DifferenceArrayOrdering, DifferenceArrayElement, DifferenceProperty, DifferenceChild};
