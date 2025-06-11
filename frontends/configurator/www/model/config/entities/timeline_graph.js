import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {DisplayableNode} from '../node_displayable.js';

export class TimelineGraph extends DisplayableNode {
	static getProperties() {
		return {
			study: {type: Entities.Study.name, back_reference: true},
			id: {type: 'string'},
			shortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			scopeModelId: {type: 'string'},
			studyStartEventModelId: {type: 'string'},
			studyStopEventModelId: {type: 'string'},
			studyPeriodIsDefault: {type: 'boolean'},
			width: {type: 'number'},
			height: {type: 'number'},
			legendWidth: {type: 'number'},
			scrollerHeight: {type: 'number'},
			showScroller: {type: 'boolean'},
			footNote: {type: 'object'},
			sections: {type: 'array', subtype: Entities.TimelineGraphSection.name}
		};
	}

	constructor(values) {
		super();
		this.study = undefined;
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
		this.scopeModelId = undefined;
		this.studyStartEventModelId = undefined;
		this.studyStopEventModelId = undefined;
		this.studyPeriodIsDefault = true;
		this.width = undefined;
		this.height = 450;
		this.legendWidth = undefined;
		this.scrollerHeight = undefined;
		this.showScroller = undefined;
		this.footNote = {};
		this.sections = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	//tree
	getChildren(entity) {
		switch(entity) {
			case Entities.TimelineGraphSection:
				return this.sections.slice();
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.TimelineGraphSection:
				this.sections.push(child);
				child.timelineGraph = this;
				break;
			default:
				throw new Error(`Entity ${child_entity.name} cannot be added as a child of entity ${this.getEntity().name}`);
		}
		EntitiesHooks?.AddChildNode.call(this, child);
	}

	//bus
	onChangeScopeModelId(event) {
		if(this.scopeModelId && this.scopeModelId === event.oldValue) {
			this.scopeModelId = event.newValue;
		}
	}
	onDeleteScopeModel(event) {
		if(this.scopeModelId === event.node.id) {
			this.scopeModelId = undefined;
		}
	}

	onDeleteTimelineGraphSection(event) {
		this.sections.removeElement(event.node);
	}
	onMoveTimelineGraphSection(event) {
		if(event.newParent === this) {
			event.node.timelineGraph.sections.removeElement(event.node);
			event.node.timelineGraph = this;
			this.sections.push(event.node);
		}
	}

	//report
	report(settings) {
		const report = super.report(settings);
		const section_ids = [];
		const zones_min = [];
		const zones_max = [];
		for(let i = this.sections.length - 1; i >= 0; i--) {
			const section = this.sections[i];
			//id uniqueness
			if(section_ids.includes(section.id)) {
				report.addError(`There is more than one section with id ${section.id} in graph ${this.id}`);
			}
			section_ids.push(section.id);
			//scales don't overlap
			if(section.scale) {
				if(section.scale.min && zones_min.includes(section.scale.min)) {
					report.addWarning(`There is more than one section having min zone set to ${section.scale.min}`);
					zones_min.push(section.scale.min);
				}
				if(section.scale.max && zones_max.includes(section.scale.max)) {
					report.addWarning(`There is more than one section having max zone set to ${section.scale.max}`);
					zones_max.push(section.scale.max);
				}
			}
		}
		return report;
	}
}
