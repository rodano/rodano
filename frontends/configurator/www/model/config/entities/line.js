import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';
import {Layout} from './layout.js';

export class Line extends Node {
	static getProperties() {
		return {
			layout: {type: Entities.Layout.name, back_reference: true},
			cells: {type: 'array', subtype: Entities.Cell.name}
		};
	}
	static getFormModelComparator () {
		return (line_1, line_2) => {
			if(line_1.layout === line_2.layout) {
				const lines = line_1.layout.lines;
				return lines.indexOf(line_1) - lines.indexOf(line_2);
			}
			const layout_comparator = Layout.getFormModelComparator();
			return layout_comparator(line_1.layout, line_2.layout);
		};
	}

	constructor(values) {
		super();
		this.layout = undefined;
		this.cells = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getLocalizedLabel() {
		return this.layout.lines.indexOf(this).toString();
	}
	generateCellId() {
		return `${this.layout.id}_${this.layout.lines.indexOf(this)}_${this.cells.length}_${new Date().getTime()}`;
	}

	//tree
	getChildren(entity) {
		switch(entity) {
			case Entities.Cell:
				return this.cells.slice();
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.Cell:
				this.cells.push(child);
				child.line = this;
				break;
			default:
				throw new Error(`Entity ${child_entity.name} cannot be added as a child of entity ${this.getEntity().name}`);
		}
		EntitiesHooks?.AddChildNode.call(this, child);
	}

	//html
	createHTML(languages, datasets) {
		const line_html = document.createElement('tr');
		line_html.appendChildren(this.cells.map(function(cell) {
			const cell_html = document.createFullElement('td', {colspan: cell.colspan || 1});
			let cell_name, value;
			if(datasets && cell.hasFieldModel()) {
				const dataset = datasets.find(d => d.datasetModelId === cell.datasetModelId);
				cell_name = `${dataset.datasetModelId}:0:${cell.fieldModelId}`;
				value = dataset.getField(cell.fieldModelId);
			}
			cell_html.appendChild(cell.createHTML(languages, cell_name, value));
			return cell_html;
		}));
		return line_html;
	}
}
