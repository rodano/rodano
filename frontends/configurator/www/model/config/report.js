import '../../basic-tools/extension.js';
import {DisplayableNode} from './node_displayable.js';
import {Utils} from './utils.js';

class Report {
	static checkLocalizedLabel(report, node, property) {
		Object.values(node[property]).forEach(l => Report.checkText(report, node, property, l));
	}

	static checkLabel(report, node, property) {
		Report.checkText(report, node, property, node[property]);
	}

	static checkText(report, node, property, text) {
		const error = Utils.checkText(text);
		if(error) {
			report.addError(`Error in property ${property} of ${node.getEntity().label} ${node.id}: ${error}`);
		}
	}

	static checkId(report, node, check_format) {
		const error = DisplayableNode.checkId(node.id, check_format);
		if(error) {
			report.addError(`${node.getEntity().label} has an invalid id ${node.id}: ${error}`);
		}
	}

	constructor(node) {
		this.node = node;
		this.infos = [];
		this.warnings = [];
		this.errors = [];
	}
	/**
	 * @param {string} info - The information
	 * @param {any} [node] - The node this information relates to (default to the node of the report)
	 * @param {Function} [patch] - A function that can fix this issue
	 * @param {string} [solution] - A description of the fix that will be applied by the function
	 */
	addInfo(info, node, patch, solution) {
		this.infos.push(new ReportMessage(info, node || this.node, patch, solution));
	}
	/**
	 * @param {string} warning - The warning
	 * @param {any} [node] - The node this information relates to (default to the node of the report)
	 * @param {Function} [patch] - A function that can fix this issue
	 * @param {string} [solution] - A description of the fix that will be applied by the function
	 */
	addWarning(warning, node, patch, solution) {
		this.warnings.push(new ReportMessage(warning, node || this.node, patch, solution));
	}
	/**
	 * @param {string} error - The error
	 * @param {any} [node] - The node this information relates to (default to the node of the report)
	 * @param {Function} [patch] - A function that can fix this issue
	 * @param {string} [solution] - A description of the fix that will be applied by the function
	 */
	addError(error, node, patch, solution) {
		this.errors.push(new ReportMessage(error, node || this.node, patch, solution));
	}
	add(report) {
		this.infos.pushAll(report.infos);
		this.warnings.pushAll(report.warnings);
		this.errors.pushAll(report.errors);
	}
	isEmpty() {
		return this.infos.isEmpty() && this.warnings.isEmpty() && this.errors.isEmpty();
	}
}

class ReportMessage {
	constructor(message, node, patch, solution) {
		this.message = message;
		this.node = node;
		this.patch = patch;
		this.solution = solution;
	}
}

export {Report, ReportMessage};
