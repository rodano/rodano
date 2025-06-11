import '../basic-tools/extension.js';

import {UI} from '../tools/ui.js';
import {Effects} from '../tools/effects.js';
import {UUID} from '../basic-tools/uuid.js';
import {ConfigHelpers, Config} from '../model_config.js';
import {bus} from '../model/config/entities_hooks.js';
import {Languages} from '../languages.js';
import {ModelData} from '../model_data.js';
import {bus_ui} from '../bus_ui.js';
import {NodeTools} from '../node_tools.js';
import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';
import {Cell} from '../model/config/entities/cell.js';
import {Line} from '../model/config/entities/line.js';
import {Column} from '../model/config/entities/column.js';
import {Layout} from '../model/config/entities/layout.js';
import {Entities} from '../model/config/entities.js';
import {MediaTypes} from '../media_types.js';

const CSS_WIDTH_REGEXP = /width ?: ?(\d+)px;/;

//cursor for cell label width
let moving_cursor;
let moving_cursor_container_position;
function start_move_cursor(event) {
	event.stop();
	document.addEventListener('mouseup', stop_move_cursor);
	document.addEventListener('mousemove', move_cursor);
	document.body.classList.add('resizing');
	moving_cursor = this;
	moving_cursor_container_position = moving_cursor.parentNode.getBoundingClientRect();
}

function move_cursor(event) {
	event.stop();
	let offset = Math.round((event.clientX - moving_cursor_container_position.left) / 5) * 5;
	if(offset < 0) {
		offset = 0;
	}
	const new_width = `width: ${offset}px;`;

	const cell_td = moving_cursor.parentNode.parentNode.parentNode;
	const cell_line = cell_td.parentNode;
	const index = cell_line.childNodes.indexOf(cell_td);
	const lines = event.shiftKey ? cell_line.parentNode.childNodes : [cell_line];

	for(let i = lines.length - 1; i >= 0; i--) {
		const line = lines[i];
		const cell_labels = line.querySelectorAll(`td:nth-child(${index + 1}) > div > label`);
		for(let j = cell_labels.length - 1; j >= 0; j--) {
			const line_cell_label = cell_labels[j];
			const line_cell = line_cell_label.parentNode.parentNode.cell;
			//update cell css code
			if(!line_cell.cssCodeForLabel) {
				line_cell.cssCodeForLabel = '';
			}
			if(CSS_WIDTH_REGEXP.test(line_cell.cssCodeForLabel)) {
				line_cell.cssCodeForLabel = line_cell.cssCodeForLabel.replace(CSS_WIDTH_REGEXP, new_width);
			}
			else {
				line_cell.cssCodeForLabel += new_width;
			}
			//update label
			line_cell_label.setAttribute('style', line_cell.cssCodeForLabel);
		}
	}
}

function stop_move_cursor(event) {
	event.stop();
	document.body.classList.remove('resizing');
	this.removeEventListener('mousemove', move_cursor);
	this.removeEventListener('mouseup', stop_move_cursor);
}

//cursor for cell label padding
let moving_padding_cursor;
let moving_padding_cursor_container_position;
function start_move_padding_cursor(event) {
	document.addEventListener('mouseup', stop_move_padding_cursor);
	document.addEventListener('mousemove', move_padding_cursor);
	document.body.classList.add('resizing');
	moving_padding_cursor = this;
	moving_padding_cursor_container_position = moving_padding_cursor.parentNode.getBoundingClientRect();
	//stop event
	event.stop();
}

function move_padding_cursor(event) {
	let offset = Math.round((event.clientX - moving_padding_cursor_container_position.left) / 5) * 5;
	if(offset < 0) {
		offset = 0;
	}
	const cell_padding_regexp = /padding-left ?: ?\d+px;/;
	const new_padding = `padding-left: ${offset}px;`;

	const cell = moving_padding_cursor.parentNode.parentNode.parentNode.cell;
	const cell_label = moving_padding_cursor.parentNode;

	//update cell css code
	if(!cell.cssCodeForLabel) {
		cell.cssCodeForLabel = '';
	}
	if(cell_padding_regexp.test(cell.cssCodeForLabel)) {
		cell.cssCodeForLabel = cell.cssCodeForLabel.replace(cell_padding_regexp, new_padding);
	}
	else {
		cell.cssCodeForLabel += new_padding;
	}
	//update label
	cell_label.setAttribute('style', cell.cssCodeForLabel);

	//stop event
	event.stopPropagation();
	event.preventDefault();
}

function stop_move_padding_cursor(event) {
	document.body.classList.remove('resizing');
	this.removeEventListener('mousemove', move_padding_cursor);
	this.removeEventListener('mouseup', stop_move_padding_cursor);
	//stop event
	event.stop();
}

//cursor for columns
//TODO apply modifications only on model and let the bus do the rest (binding between the ui and the model)
let moving_column_cursor;
let moving_column_cursor_container_position;
function start_move_column_cursor(event) {
	document.addEventListener('mouseup', stop_move_column_cursor);
	document.addEventListener('mousemove', move_column_cursor);
	document.body.classList.add('resizing');
	moving_column_cursor = this;
	moving_column_cursor_container_position = moving_column_cursor.parentNode.getBoundingClientRect();
	//stop event
	event.stop();
}

function move_column_cursor(event) {
	let offset = Math.round((event.clientX - moving_column_cursor_container_position.left) / 5) * 5;
	if(offset < 0) {
		offset = 0;
	}
	const new_width = `width: ${offset}px;`;

	const column_th = moving_column_cursor.parentNode;
	const columns_line = column_th.parentNode;
	const index = columns_line.children.indexOf(column_th);
	const column_col = columns_line.parentNode.parentNode.querySelector(`col:nth-child(${index + 1})`);
	//update column css code
	const column = column_th.column;
	if(!column.cssCode) {
		column.cssCode = '';
	}
	if(CSS_WIDTH_REGEXP.test(column.cssCode)) {
		column.cssCode = column.cssCode.replace(CSS_WIDTH_REGEXP, new_width);
	}
	else {
		column.cssCode += new_width;
	}
	//update column width
	column_col.style.width = `${offset}px`;
	//update css
	column_th.setAttribute('style', column.cssCode);
	//stop event
	event.stop();
}

function stop_move_column_cursor(event) {
	document.body.classList.remove('resizing');
	this.removeEventListener('mousemove', move_column_cursor);
	this.removeEventListener('mouseup', stop_move_column_cursor);
	//stop event
	event.stop();
}

//move cells
function retrieve_dragged_node(event) {
	const dragged_node_id = event.dataTransfer.getData(MediaTypes.NODE_GLOBAL_ID);
	if(dragged_node_id) {
		return selected_form_model.study.getNode(dragged_node_id);
	}
	return undefined;
}

function check_valid_node(node) {
	return node && [Entities.FieldModel, Entities.Cell].includes(node.getEntity());
}

function cell_dragstart(event) {
	this.style.opacity = 0.6;
	event.dataTransfer.effectAllowed = 'move';
	event.dataTransfer.setData('text/plain', this.cell.id);
	event.dataTransfer.setData(MediaTypes.NODE_GLOBAL_ID, this.cell.getGlobalId());
}

function cell_dragover(event) {
	if(event.dataTransfer.types.includes(MediaTypes.NODE_GLOBAL_ID)) {
		let allow_drop = false;
		//BUG some browsers do not allow to check what is dragged during drag https://bugs.webkit.org/show_bug.cgi?id=58206 or http://code.google.com/p/chromium/issues/detail?id=50009
		//check if drop is possible only on browsers that allow sniffing data
		if(event.dataTransfer.getData(MediaTypes.NODE_GLOBAL_ID)) {
			const node = retrieve_dragged_node(event);
			if(check_valid_node(node) && node !== this.cell) {
				allow_drop = true;
			}
		}
		//SHORTCUT allow drop for browsers that don't allow sniffing data
		else {
			console.warn('Unable to sniff data, allowing item drop');
			allow_drop = true;
		}
		//ENDSHORTCUT
		if(allow_drop) {
			this.classList.add('dragover');
			event.preventDefault();
		}
	}
}

function cell_dragenter(event) {
	const node = retrieve_dragged_node(event);
	if(check_valid_node(node) && node !== this.cell) {
		this.classList.add('dragover');
	}
}

function cell_dragleave() {
	this.classList.remove('dragover');
}

function cell_drop(event) {
	const node = retrieve_dragged_node(event);
	if(check_valid_node(node) && node !== this.cell) {
		event.preventDefault();
		this.classList.remove('dragover');
		if(node.getEntity() === Entities.Cell) {
			//update model
			//preserve colspan
			const colspan = node.colspan;
			const target_colspan = this.cell.colspan;
			node.colspan = target_colspan;
			this.cell.colspan = colspan;

			//move in lines
			const dragged_line = node.line;
			const line = this.cell.line;
			const index = line.cells.indexOf(this.cell);
			dragged_line.cells[dragged_line.cells.indexOf(node)] = this.cell;
			line.cells[index] = node;
			this.cell.line = dragged_line;
			node.line = line;

			//update ui for dragged cell if it is in the same form model
			if(node.line.layout.formModel === this.cell.line.layout.formModel) {
				//find dragged cell in ui
				const dragged_cell_ui = document.querySelectorAll('#form_model_layouts tbody > tr > td:not(:first-child):not(:last-child)').find(e => e.cell === node);
				dragged_cell_ui.parentNode.replaceChild(draw_cell(this.cell), dragged_cell_ui);
			}

			//update ui for dropped cell
			this.parentNode.replaceChild(draw_cell(node), this);
		}
		else if(node.getEntity() === Entities.FieldModel) {
			this.cell.id = node.generateCellId(this.cell.line.layout.formModel);
			this.cell.datasetModelId = node.datasetModel.id;
			this.cell.fieldModelId = node.id;
			this.parentNode.replaceChild(draw_cell(this.cell), this);
		}
	}
}

function cell_dragend() {
	this.style.opacity = 1;
}

function draw_cell(cell) {
	//build cell
	const properties = {draggable: true, 'class': 'cell'};
	if(cell.colspan) {
		properties.colspan = cell.colspan;
	}
	const cell_td = document.createFullElement('td', properties);
	cell_td.cell = cell;
	cell_td.addEventListener('dragstart', cell_dragstart);
	cell_td.addEventListener('dragend', cell_dragend);
	cell_td.addEventListener('dragenter', cell_dragenter);
	cell_td.addEventListener('dragover', cell_dragover);
	cell_td.addEventListener('dragleave', cell_dragleave);
	cell_td.addEventListener('drop', cell_drop);
	cell_td.addEventListener('mouseover', mouseover_cell);
	cell_td.addEventListener('mouseout', mouseout_cell);

	const cell_div = document.createFullElement('div', {style: 'position : relative;'});

	//text before
	const cell_text_before = document.createFullElement('div', {class: 'text'});
	cell_div.appendChild(cell_text_before);
	if(!Object.isEmpty(cell.textBefore)) {
		cell_text_before.innerHTML = cell.getLocalizedTextBefore(Languages.GetLanguage());
	}

	//content
	if(cell.hasFieldModel()) {
		const field_model = cell.getFieldModel();
		const client_id = `${cell.datasetModelId}_${cell.fieldModelId}_${UUID.Generate()}`;

		//label
		const cell_label = document.createFullElement(
			'label',
			{'for': client_id, 'style': `width: 140px; ${cell.cssCodeForLabel}; display: ${cell.displayLabel ? 'block' : 'none'};`}
		);

		const cell_padding_control = document.createFullElement('span', {title: 'Move to offset label', 'class': 'cursor', style: 'float: left;'});
		cell_padding_control.addEventListener('mousedown', start_move_padding_cursor);
		cell_label.appendChild(cell_padding_control);
		cell_label.appendChild(NodeTools.Draw(field_model, Config.Enums.LabelType.SHORT));

		const cell_control = document.createFullElement('span', {title: 'Move to resize label, hold shift to resize all labels', 'class': 'cursor', style: 'float: right;'});
		cell_control.addEventListener('mousedown', start_move_cursor);
		cell_label.appendChild(cell_control);

		//field
		const cell_field = field_model.createHTML(cell, Languages.GetLanguage(), client_id);
		if(cell.cssCodeForInput) {
			cell_field.setAttribute('style', cell.cssCodeForInput);
		}

		//append field before label for checkboxes
		if(field_model.type === 'CHECKBOX') {
			cell_field.style.float = 'left';
			cell_div.appendChild(cell_field);
			cell_div.appendChild(cell_label);
		}
		//append label before field for all other types
		else {
			cell_div.appendChild(cell_label);
			cell_div.appendChild(cell_field);
		}
	}

	//text after
	const cell_text_after = document.createFullElement('div', {class: 'text'});
	cell_div.appendChild(cell_text_after);
	if(!Object.isEmpty(cell.textAfter)) {
		cell_text_after.innerHTML = cell.getLocalizedTextAfter(Languages.GetLanguage());
	}

	//add icon if cell is constrained
	if(!Object.isEmpty(cell.constraint)) {
		cell_div.appendChild(document.createFullElement('img', {src: 'images/cog.png', alt: 'Constrained', title: 'Cell is constrained', style: 'position: absolute; right: 0.5rem; top: 2px;'}));
	}

	//management buttons
	const edit_cell_button = document.createFullElement('a', {href: `#node=${cell.getGlobalId()}`, title: 'Edit cell', 'class': 'action', style: 'right : 51px;'});
	edit_cell_button.appendChild(document.createFullElement('img', {src: 'images/magnifier.png', alt: 'Edit cell'}));
	cell_div.appendChild(edit_cell_button);

	const extend_cell_button = document.createFullElement('a', {href: '#', title: 'Extend cell', 'class': 'action', style: 'right : 26px;'});
	extend_cell_button.appendChild(document.createFullElement('img', {src: 'images/arrow_ew.png', alt: 'Extend cell'}));
	extend_cell_button.addEventListener(
		'click',
		function(event) {
			event.stop();
			const line = cell.line;
			const index = line.cells.indexOf(cell);
			//extend cell is only possible if cell is not the last cell in the row and if next cell in the row is empty
			if(index < line.cells.length - 1 && line.cells[index + 1].isEmpty()) {
				cell.colspan += (line.cells[index + 1].colspan ? line.cells[index + 1].colspan : 1);
				line.cells.remove(index + 1);
				//redraw all layout
				const form_model_layout_div = cell_td.parentNode.parentNode.parentNode.parentNode;
				form_model_layout_div.parentNode.replaceChild(draw_layout(line.layout), form_model_layout_div);
			}
			else {
				UI.Notify('Unable to extend a cell if it is the last cell or if next cell is not empty', {tag: 'error', icon: 'images/notifications_icons/warning.svg'});
			}
		}
	);
	cell_div.appendChild(extend_cell_button);

	const reset_cell_button = document.createFullElement('a', {href: '#', title: 'Clean', 'class': 'action', style: 'right : 1px;'});
	reset_cell_button.appendChild(document.createFullElement('img', {src: 'images/bin.png', alt: 'Clean'}));
	reset_cell_button.addEventListener(
		'click',
		function(event) {
			event.stop();
			UI.Validate('Are you sure you want to reset this cell?').then(confirmed => {
				if(confirmed) {
					//update model
					const line = cell.line;
					const index = line.cells.indexOf(cell);
					const new_cell = new Cell({
						id: cell.id,
						colspan: cell.colspan,
						line: line
					});
					line.cells[index] = new_cell;
					cell.delete();
					//update ui
					const child_nodes = Array.prototype.slice.call(cell_div.childNodes);
					for(let i = child_nodes.length - 1; i >= 0; i--) {
						const child_node = child_nodes[i];
						if(![edit_cell_button, extend_cell_button, reset_cell_button].includes(child_node)) {
							cell_div.removeChild(child_node);
						}
					}
					cell_td.cell = new_cell;
				}
			});
		}
	);
	cell_div.appendChild(reset_cell_button);

	cell_td.appendChild(cell_div);
	return cell_td;
}

function mouseover_cell() {
	//build targets list
	const targets = this.cell.visibilityCriteria.map(vc => vc.targetCellIds).flat();
	//highlight cells
	const that = this;
	this.parentNode.parentNode.querySelectorAll('td').filter(e => e.cell).forEach(function(element) {
		let highlight = false;
		//cell is a target
		if(targets.includes(element.cell.id)) {
			highlight = true;
		}
		//the mouse-overed cell is a target
		else {
			highlight = element.cell.visibilityCriteria.some(c => c.targetCellIds.includes(that.cell.id));
		}
		//highlight current cell and all target cells
		if(highlight || element === that) {
			element.classList.add('highlight');
		}
	});
}

function mouseout_cell() {
	//unhighlight all cells
	this.parentNode.parentNode.querySelectorAll('td').filter(e => e.cell).forEach(e => e.classList.remove('highlight'));
}

function draw_layout(layout) {
	const layout_div = document.createFullElement('div', {'class': 'sortable'});
	layout_div.layout = layout;

	const layout_title = document.createFullElement('h3');

	layout_title.appendChild(document.createFullElement('img', {src: 'images/arrows_up_down.png', alt: 'Sort layout', title: 'Sort layout', style: 'cursor: pointer; vertical-align: middle;'}));

	//add icon if layout is conditioned
	if(!Object.isEmpty(layout.conditions)) {
		layout_title.appendChild(document.createFullElement('img', {src: 'images/cog.png', alt: 'Conditioned', title: 'Layout is conditioned', style: 'float: right; margin-top: 1px;'}));
	}

	//layout label
	const layout_label = document.createFullElement('span', {}, layout.id);
	layout_title.appendChild(layout_label);

	//edit layout button
	const edit_layout_button = document.createFullElement('a', {href: `#node=${layout.getGlobalId()}`, title: 'Edit layout'});
	edit_layout_button.appendChild(document.createFullElement('img', {src: 'images/magnifier.png', alt: 'Edit layout'}));
	layout_title.appendChild(edit_layout_button);

	//duplicate layout button
	const duplicate_layout_button = document.createFullElement('a', {href: '#', title: 'Duplicate layout'});
	duplicate_layout_button.appendChild(document.createFullElement('img', {src: 'images/page_copy.png', alt: 'Duplicate layout'}));
	duplicate_layout_button.addEventListener(
		'click',
		function(event) {
			event.stop();
			//find new id
			let layout_id = layout.id;
			while(layout.formModel.layouts.find(l => l.id === layout_id)) {
				layout_id = `COPY_${layout_id}`;
			}
			//reviver callback
			function layout_reviver_callback(node) {
				if(node.getEntity() === Entities.Cell) {
					//modify cell id
					node.id = `COPY_${node.id}`;
					//remove visibility criteria
					node.visibilityCriteria = [];
				}
			}
			const new_layout = ConfigHelpers.CloneNode(layout, {id: layout_id, formModel: layout.formModel}, layout_reviver_callback);
			new_layout.formModel.layouts.push(new_layout);
			document.getElementById('form_model_layouts').appendChild(draw_layout(new_layout));
		}
	);
	layout_title.appendChild(duplicate_layout_button);

	//delete layout button
	const delete_layout_button = document.createFullElement('a', {href: '#', title: 'Delete layout'});
	delete_layout_button.appendChild(document.createFullElement('img', {src: 'images/cross.png', alt: 'Delete layout'}));
	delete_layout_button.addEventListener(
		'click',
		function(event) {
			event.stop();
			UI.Validate('Are you sure you want to delete this layout?').then(confirmed => {
				if(confirmed) {
					layout.delete();
					document.getElementById('form_model_layouts').removeChild(this.parentNode.parentNode);
				}
			});
		}
	);
	layout_title.appendChild(delete_layout_button);

	layout_div.appendChild(layout_title);

	//text before
	const layout_text_before = document.createFullElement('div', {class: 'text'});
	layout_div.appendChild(layout_text_before);
	if(!Object.isEmpty(layout.textBefore)) {
		layout_text_before.innerHTML = layout.getLocalizedTextBefore(Languages.GetLanguage());
	}

	const paragraph = document.createFullElement('p', {'class': 'inline', 'style': 'padding-left: 22px;'});
	layout_div.appendChild(paragraph);
	const add_layout_line = document.createFullElement('button', {type: 'button'}, 'Add line');
	add_layout_line.addEventListener(
		'click',
		function() {
			const line = new Line();
			line.layout = layout;
			layout.lines.push(line);
			for(let i = 0; i < layout.columns.length; i++) {
				const cell = new Cell({id: line.generateCellId(), line: line});
				line.cells.push(cell);
			}
			//hmm memory leak?
			layout_div.parentNode.replaceChild(draw_layout(layout), layout_div);
		}
	);
	paragraph.appendChild(add_layout_line);

	const add_layout_column = document.createFullElement('button', {type: 'button', style: 'margin-left: 0.5rem;'}, 'Add column');
	add_layout_column.addEventListener(
		'click',
		function() {
			const column = new Column({layout: layout});
			layout.columns.push(column);
			for(let i = 0; i < layout.lines.length; i++) {
				const cell = new Cell({id: layout.lines[i].generateCellId(), line: layout.lines[i]});
				layout.lines[i].cells.push(cell);
			}
			//same here
			layout_div.parentNode.replaceChild(draw_layout(layout), layout_div);
		}
	);
	paragraph.appendChild(add_layout_column);

	const layout_table = document.createFullElement('table', {style: 'border-collapse: collapse; border-spacing: 0;'});
	layout_div.appendChild(layout_table);

	const layout_colgroup = document.createElement('colgroup');
	layout_colgroup.appendChild(document.createFullElement('col', {style: 'width: 18px;'}));
	for(let i = 0; i < layout.columns.length; i++) {
		const column = layout.columns[i];
		const column_col = document.createFullElement('col');
		column_col.column = column;
		//set width if present
		if(column.cssCode) {
			const match = column.cssCode.match(CSS_WIDTH_REGEXP);
			if(match) {
				column_col.style.width = `${match[1]}px`;
			}
		}
		layout_colgroup.appendChild(column_col);
	}
	layout_colgroup.appendChild(document.createFullElement('col', {style: 'width: 15px;'}));
	layout_table.appendChild(layout_colgroup);

	const layout_table_header = document.createFullElement('thead');
	layout_table.appendChild(layout_table_header);

	//columns
	const layout_line = document.createElement('tr');
	//sort line handles column
	layout_line.appendChild(document.createFullElement('th'));
	for(let i = 0; i < layout.columns.length; i++) {
		const column = layout.columns[i];
		const column_th = document.createFullElement('th', {'class': 'column', style: column.cssCode});
		column_th.column = column;

		const column_control = document.createFullElement('span', {title: 'Move to resize column', 'class': 'cursor', style: 'float: right;'});
		column_control.addEventListener('mousedown', start_move_column_cursor);
		column_th.appendChild(column_control);

		//delete column button
		const delete_column_button = document.createFullElement('a', {href: '#', title: 'Delete column', style: 'margin-right: 0.5rem;'});
		delete_column_button.appendChild(document.createFullElement('img', {src: 'images/cross.png', alt: 'Delete column'}));
		delete_column_button.addEventListener(
			'click',
			function(event) {
				event.stop();
				const column = this.parentNode.column;
				if(column.layout.columns.length > 1) {
					UI.Validate('Are you sure you want to delete this column?').then(confirmed => {
						if(confirmed) {
							column.delete();
							layout_div.parentNode.replaceChild(draw_layout(layout), layout_div);
						}
					});
				}
				else {
					UI.Notify('Unable to remove last column', {tag: 'error', icon: 'images/notifications_icons/warning.svg'});
				}
			}
		);
		column_th.appendChild(delete_column_button);

		//edit column button
		const edit_column_button = document.createFullElement('a', {href: `#node=${column.getGlobalId()}`, title: 'Edit column'});
		edit_column_button.appendChild(document.createFullElement('img', {src: 'images/magnifier.png', alt: 'Edit column'}));
		column_th.appendChild(edit_column_button);

		layout_line.appendChild(column_th);
	}
	//delete line column
	layout_line.appendChild(document.createFullElement('th'));
	layout_table_header.appendChild(layout_line);

	const layout_table_body = document.createFullElement('tbody');
	layout_table.appendChild(layout_table_body);

	//lines
	for(let i = 0; i < layout.lines.length; i++) {
		const line = layout.lines[i];
		const layout_line = document.createFullElement('tr', {'class': 'line'});
		layout_line.line = line;

		//drag button
		const layout_line_move = document.createFullElement('td', {draggrable: true});
		layout_line_move.appendChild(document.createFullElement('img', {src: 'images/arrows_up_down.png', alt: 'Sort line', title: 'Sort line', style: 'cursor: pointer; vertical-align: middle;'}));
		layout_line.appendChild(layout_line_move);

		//cells
		line.cells.map(draw_cell).forEach(Node.prototype.appendChild, layout_line);

		//delete button
		const delete_layout_line = document.createFullElement('img', {src: 'images/cross.png', alt: 'Delete line', title: 'Delete line', style: 'cursor: pointer; vertical-align: text-bottom;'});
		delete_layout_line.addEventListener(
			'click',
			function(event) {
				event.stop();
				const line = this.parentNode.parentNode.line;
				if(line.layout.lines.length > 1) {
					UI.Validate('Are you sure you want to delete this line?').then(confirmed => {
						if(confirmed) {
							line.layout.lines.removeElement(line);
							layout_div.parentNode.replaceChild(draw_layout(layout), layout_div);
						}
					});
				}
				else {
					UI.Notify('Unable to remove last line', {tag: 'error', icon: 'images/notifications_icons/warning.svg'});
				}
			}
		);
		const delete_layout_line_cell = document.createFullElement('td', {'class': 'control'});
		delete_layout_line_cell.appendChild(delete_layout_line);
		layout_line.appendChild(delete_layout_line_cell);
		layout_table_body.appendChild(layout_line);
	}

	//text after
	const layout_text_after = document.createFullElement('div', {class: 'text'});
	layout_div.appendChild(layout_text_after);
	if(!Object.isEmpty(layout.textAfter)) {
		layout_text_after.innerHTML = layout.getLocalizedTextAfter(Languages.GetLanguage());
	}

	Effects.Sortable(
		layout_table_body,
		function() {
			//update model
			layout.lines = layout_table_body.children.map(e => e.line);
			//update all cells links
			layout_table_body.querySelectorAll('td').filter(e => e.cell !== undefined).forEach(function(cell_ui) {
				cell_ui.querySelector('a[title="Edit cell"]').setAttribute('href', `#node=${cell_ui.cell.getGlobalId()}`);
			});
		},
		'td:first-child > img'
	);

	return layout_div;
}

let selected_form_model;

export default {
	form: 'edit_form_model_form',
	init: function() {
		document.getElementById('edit_form_model_form').addEventListener('submit', FormStaticActions.SubmitEditionForm);

		function bus_register() {
			bus.register({
				onChangeCell: function(event) {
					if(event.node.line.layout.formModel === selected_form_model && ['id', 'cssCodeForLabel', 'displayLabel', 'textBefore', 'textAfter'].includes(event.property)) {
						const cell_ui = document.querySelectorAll('#form_model_layouts td').find(e => e.cell === event.node);
						//update link
						cell_ui.querySelector('a[title="Edit cell"]').setAttribute('href', `#node=${event.node.getGlobalId()}`);
						//update css for cells with an attribute
						if(event.node.hasFieldModel()) {
							cell_ui.querySelector('label').setAttribute('style', `width: 140px; ${event.node.cssCodeForLabel}; display: ${event.node.displayLabel ? 'block' : 'none'};`);
						}
						//update text zones
						if(!Object.isEmpty(event.node.textBefore)) {
							cell_ui.querySelector('div > .text:first-of-type').innerHTML = event.node.getLocalizedTextBefore(Languages.GetLanguage());
						}
						if(!Object.isEmpty(event.node.textAfter)) {
							cell_ui.querySelector('div > .text:last-of-type').innerHTML = event.node.getLocalizedTextAfter(Languages.GetLanguage());
						}
					}
				},
				onChangeColumn: function(event) {
					if(event.node.layout.formModel === selected_form_model && ['cssCode'].includes(event.property)) {
						const column_th = document.querySelectorAll('#form_model_layouts th').find(e => e.column === event.node);
						//update link
						column_th.querySelector('a[title="Edit column"]').setAttribute('href', `#node=${event.node.getGlobalId()}`);
						//update css
						column_th.setAttribute('style', event.node.cssCode);
						const column_col = document.querySelectorAll('#form_model_layouts col').find(e => e.column === event.node);
						//update width if present
						const match = event.node.cssCode.match(CSS_WIDTH_REGEXP);
						if(match) {
							column_col.style.width = `${match[1]}px`;
						}
					}
				},
				onChangeLayout: function(event) {
					if(event.node.formModel === selected_form_model && ['id', 'textBefore', 'textAfter'].includes(event.property)) {
						const layout_ui = document.querySelectorAll('#form_model_layouts > div').find(e => e.layout === event.node);
						//update id
						layout_ui.querySelector('h3 > span').textContent = event.node.id;
						//update link
						layout_ui.querySelector('h3 > a[title="Edit layout"]').setAttribute('href', `#node=${event.node.getGlobalId()}`);
						//update all columns links
						document.querySelectorAll('#form_model_layouts th').filter(e => e.column !== undefined).forEach(function(column_ui) {
							column_ui.querySelector('a[title="Edit column"]').setAttribute('href', `#node=${column_ui.column.getGlobalId()}`);
						});
						//update all cells links
						document.querySelectorAll('#form_model_layouts td').filter(e => e.cell !== undefined).forEach(function(cell_ui) {
							cell_ui.querySelector('a[title="Edit cell"]').setAttribute('href', `#node=${cell_ui.cell.getGlobalId()}`);
						});
						//update text zones
						if(!Object.isEmpty(event.node.textBefore)) {
							layout_ui.querySelector('div > .text:first-of-type').innerHTML = event.node.getLocalizedTextBefore(Languages.GetLanguage());
						}
						if(!Object.isEmpty(event.node.textAfter)) {
							layout_ui.querySelector('div > .text:last-of-type').innerHTML = event.node.getLocalizedTextAfter(Languages.GetLanguage());
						}
					}
				}
			});
		}
		//register hook when a study is loaded because the bus will be reset at that time
		bus_ui.register({onLoadStudy: bus_register});
		bus_register();

		document.getElementById('form_model_layout_add').addEventListener(
			'click',
			function(event) {
				event.stop();
				//find next layout id
				let layout_id_index = 1;
				let layout_id;
				//increment layout index while there is already an existing layout with generated id
				do {
					layout_id = `LAYOUT_${layout_id_index++}`;
				}
				while(selected_form_model.layouts.some(l => l.id === layout_id));
				//layout
				const layout = new Layout({id: layout_id, formModel: selected_form_model});
				selected_form_model.layouts.push(layout);
				//column
				const column = new Column({layout: layout});
				layout.columns.push(column);
				//line
				const line = new Line({layout: layout});
				layout.lines.push(line);
				//cell
				const cell = new Cell({id: line.generateCellId(), line: line});
				line.cells.push(cell);

				document.getElementById('form_model_layouts').appendChild(draw_layout(layout));
			}
		);

		document.getElementById('form_model_layout_generate').addEventListener(
			'click',
			function(event) {
				event.stop();
				const dataset_model_id = /**@type {HTMLSelectElement}*/ (document.getElementById('form_model_layout_generate_select')).value;
				if(dataset_model_id) {
					const layout = selected_form_model.study.getDatasetModel(dataset_model_id).generateLayout(selected_form_model);
					layout.formModel = selected_form_model;
					selected_form_model.layouts.push(layout);
					document.getElementById('form_model_layouts').appendChild(draw_layout(layout));
				}
				else {
					UI.Notify('Select a dataset model', {tag: 'error', icon: 'images/notifications_icons/warning.svg'});
				}
			}
		);

		Effects.Sortable(
			document.getElementById('form_model_layouts'),
			function() {
				selected_form_model.layouts = this.children.map(c => c.layout);
			},
			'h3 > img:first-child'
		);

		FormStaticActions.ManageConstraintEdition('constraint', document.getElementById('form_model_constraint_add'), document.getElementById('form_model_constraint_edit'), document.getElementById('form_model_constraint_delete'));
	},
	open: function(form_model) {
		selected_form_model = form_model;

		FormHelpers.FillPalette(document.getElementById('form_model_workflow_ids'), form_model.study.workflows);
		FormHelpers.FillLocalizedInput(document.getElementById('form_model_shortname'), form_model.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('form_model_longname'), form_model.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('form_model_description'), form_model.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('form_model_print_button_label'), form_model.study.languages);
		FormHelpers.UpdateForm(document.getElementById('edit_form_model_form'), form_model);

		FormHelpers.FillSelect(document.getElementById('form_model_layout_generate_select'), form_model.study.datasetModels, true);
		form_model.layouts.map(draw_layout).forEach(Node.prototype.appendChild, document.getElementById('form_model_layouts').empty());

		NodeTools.DrawUsage(form_model, document.getElementById('form_model_usage'));

		FormStaticActions.DrawRules(form_model, form_model.rules, form_model.constructor.RuleEntities, document.getElementById('form_model_saved_rules'), 'Saved rules');

		//draw form model insight
		const form_model_insight = document.getElementById('edit_form_model_insight');
		//create fake datasets to display one line in grids
		const datasets = form_model.getDatasetModels().map(function(dataset_model) {
			const constructor = ModelData.GetModel().Entities.Dataset;
			return new constructor({datasetModelId: dataset_model.id});
		});
		form_model_insight.empty();
		form_model_insight.appendChild(form_model.createHTML(Languages.GetLanguage(), datasets, ModelData.GetModel()));

		FormStaticActions.UpdateConstraintEdition(form_model, 'constraint', document.getElementById('form_model_constraint_add'), document.getElementById('form_model_constraint_edit'), document.getElementById('form_model_constraint_delete'));
	}
};
