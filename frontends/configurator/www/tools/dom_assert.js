import {Assert} from './assert.js';

export class DOMAssert extends Assert {
	constructor(doc, debug) {
		super(debug);
		this.document = doc || document;
	}
	get(selector) {
		return String.isString(selector) ? this.document.querySelector(selector) : selector;
	}
	selectContains(selector, value, message) {
		const element = this.get(selector);
		if(element.childNodes.find(n => n.getAttribute('value') === value)) {
			this.success(message || (`Select contains ${value}`));
		}
		else {
			this.fail(message || (`Select does not contain ${value}`));
		}
	}
	selectNotContains(selector, value, message) {
		const element = this.get(selector);
		if(element.childNodes.find(n => n.getAttribute('value') === value)) {
			this.fail(message || (`Select contains ${value}`));
		}
		else {
			this.success(message || (`Select does not contain ${value}`));
		}
	}
	/**
	 * Checks if the selected element is hidden (using its display and visibility property)
	 * This does not prove that the selected element is really hidden from the user perspective:
	 * - it does not check if one ancestor element of the selected element is hidden
	 * - it does not check if an other element with an absolute position is hiding the selected element
	 * @param {string | HTMLElement} selector The element or the selector to check
	 * @param {string} message A message describing the check
	 */
	hidden(selector, message) {
		const element = this.get(selector);
		//check inline style
		if(element.style.display === 'none' || element.style.visibility === 'hidden') {
			this.success(message || 'Element is hidden');
			return;
		}
		//check css
		const css = this.document.defaultView.getComputedStyle(element, undefined);
		if(css.getPropertyValue('display') === 'none' || css.getPropertyValue('visibility') === 'hidden') {
			this.success(message || 'Element is hidden');
			return;
		}
		this.fail(message || 'Element is not hidden');
	}
	/**
	 * Checks if the selected element is visible (using its display and visibility property)
	 * This does not prove that the selected element is really visible from the user perspective:
	 * - it does not check if all ancestor elements of the selected element are visible
	 * - it does not check if an other element with an absolute position is hiding selected element
	 * @param {string | HTMLElement} selector The element or the selector to check
	 * @param {string} message A message describing the check
	 */
	visible(selector, message) {
		const element = this.get(selector);
		//check css
		const css = this.document.defaultView.getComputedStyle(element, undefined);
		if(css.getPropertyValue('display') !== 'none' && css.getPropertyValue('visibility') !== 'hidden') {
			//check inline style
			if(element.style.display !== 'none' && element.style.visibility !== 'hidden') {
				this.success(message || 'Element is visible');
				return;
			}
		}
		this.fail(message || 'Element is not visible');
	}
	/**
	 * Checks if the selected dialog is open
	 * @param {string | HTMLDialogElement} selector The dialog or the selector to check
	 * @param {string} message A message describing the check
	 */
	open(selector, message) {
		this.ok(this.get(selector).open, message);
	}
	/**
	 * Checks if the selected dialog is closed
	 * @param {string | HTMLDialogElement} selector The dialog or the selector to check
	 * @param {string} message A message describing the check
	 */
	closed(selector, message) {
		this.notOk(this.get(selector).open, message);
	}
}
