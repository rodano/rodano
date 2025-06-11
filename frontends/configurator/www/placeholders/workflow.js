import {MediaTypes} from '../media_types.js';
import {Entities} from '../model/config/entities.js';
import {NodeTools} from '../node_tools.js';

let selected_node;

//draw columns
//order can be a number or undefined
function draw_column(order) {
	const column = document.createFullElement('div', {'data-column-order': order});
	column.appendChild(document.createFullElement('h3', {}, `Order ${Number.isNumber(order) ? order : 'undefined'}`));
	const column_workflows = document.createFullElement('ul');
	column.appendChild(column_workflows);
	//retrieve all workflows that match order if workflow order is a number
	//otherwise, group all workflows that have an undefined or empty order
	selected_node.workflows.filter(w => order !== undefined && w.orderBy === order || order === undefined && !w.orderBy).map(draw_workflow).forEach(Node.prototype.appendChild, column_workflows);
	column.addEventListener('dragenter', column_dragover);
	column.addEventListener('dragover', column_dragover);
	column.addEventListener('dragleave', column_dragleave);
	column.addEventListener('drop', column_drop);
	return column;
}

function draw_workflow(workflow) {
	const workflow_li = document.createFullElement('li');
	workflow_li.appendChild(NodeTools.Draw(workflow));
	return workflow_li;
}

function check_drop_allowed(column, event) {
	if(event.dataTransfer.types.includes(MediaTypes.NODE_GLOBAL_ID)) {
		//BUG some browsers do not allow to check what is dragged during drag
		if(event.dataTransfer.getData(MediaTypes.NODE_GLOBAL_ID)) {
			const node = selected_node.getNode(event.dataTransfer.getData(MediaTypes.NODE_GLOBAL_ID));
			//check entries contains the item and that it is not already in the list
			return node.getEntity() === Entities.Workflow && !column.querySelectorAll('a').map(a => a.dataset.nodeId).includes(node.id);
		}
		//SHORTCUT always allow drop on these browsers
		else {
			console.warn('Unable to sniff data, allowing item drop');
			return true;
		}
		//ENDSHORTCUT
	}
	return false;
}

function column_dragover(event) {
	if(check_drop_allowed(this, event)) {
		event.preventDefault();
		event.dataTransfer.dropEffect = 'move';
		this.classList.add('highlight');
	}
}

function column_dragleave() {
	this.classList.remove('highlight');
}

function column_drop(event) {
	event.preventDefault();
	//update style whatever happens
	column_dragleave.call(this);
	if(check_drop_allowed(this, event)) {
		const workflow = selected_node.getNode(event.dataTransfer.getData(MediaTypes.NODE_GLOBAL_ID));
		//update model
		workflow.orderBy = parseInt(this.dataset.columnOrder);
		draw_columns();
	}
}

function draw_columns() {
	//find all different orders that are numbers (do not consider undefined or empty orders)
	const orders = selected_node.workflows.map(w => w.orderBy).filter(Number.isNumber).reduce((a, o) => {a.includes(o) || a.push(o); return a;}, []);
	orders.sort((o1, o2) => o1 - o2);
	//add an empty order greater than maximum and an undefined order
	orders.push(orders.last() + 5);
	orders.push(undefined);
	orders.map(draw_column).forEach(Node.prototype.appendChild, document.getElementById('nodes_workflows_sort').empty());
}

export default {
	open: function(node) {
		selected_node = node;

		draw_columns();
	}
};
