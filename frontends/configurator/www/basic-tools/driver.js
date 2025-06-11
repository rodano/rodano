function mouse_event_properties(window, element) {
	const properties = {
		view: window,
		bubbles: true,
		cancelable: true,
		detail: 1
	};
	//try to put the event at the right position
	//this may not always been possible if the element does not appear on screen (due to a scroll for example)
	const rect = element.getBoundingClientRect();
	if(rect.x > 0) {
		properties.clientX = rect.x;
	}
	if(rect.y > 0) {
		properties.clientY = rect.y;
	}
	return properties;
}

export class Driver {
	constructor(win, doc) {
		this.window = win || window;
		this.document = doc || document;
		this.scripts = [];
	}
	throwError(error) {
		throw new this.window.Error(error);
	}
	#triggerChange(element) {
		const change = new this.window.UIEvent('change', {bubbles: true, cancelable: true});
		element.dispatchEvent(change);
	}
	#triggerInput(element) {
		const input = new this.window.InputEvent('input', {bubbles: true, cancelable: true});
		element.dispatchEvent(input);
	}
	#triggerKeydown(element, key) {
		const keydown = new this.window.KeyboardEvent('keydown', {key: key, bubbles: true, cancelable: true});
		element.dispatchEvent(keydown);
	}
	#triggerKeypress(element, key) {
		const keydown = new this.window.KeyboardEvent('keypress', {key: key, bubbles: true, cancelable: true});
		element.dispatchEvent(keydown);
	}
	#triggerKeyup(element, key) {
		const keydown = new this.window.KeyboardEvent('keyup', {key: key, bubbles: true, cancelable: true});
		element.dispatchEvent(keydown);
	}
	//find an element in the page but does not check if it's visible
	async find(selector) {
		return new this.window.Promise((resolve, reject) => {
			//exclude empty selector
			if(!selector) {
				this.throwError('A valid selector or a HTMLElement must provided');
				reject();
			}

			//if selector is a string (as it should be), retrieve the associated element in the dom
			if(String.isString(selector)) {
				//try to find element
				const element = this.document.querySelector(selector);
				if(!element) {
					this.throwError(`No element match selector ${selector}`);
				}
				resolve(element);
			}
			//consider that the selector is already an element
			else {
				resolve(selector);
			}
		});
	}
	//get an element in the page
	//element must be visible except if hidden is set to true in options
	async get(selector, options) {
		const element = await this.find(selector);
		if(!options || !options.hidden) {
			//check if element is visible
			//this is a first quick test that works most of the time, but not for fixed elements
			if(!element.offsetParent) {
				//do advanced check that takes more time but is reliable
				const style = this.window.getComputedStyle(element);
				if(style.display === 'none') {
					this.throwError(`Element is not visible ${selector}`);
				}
			}
		}
		return element;
	}
	async getShadow(selector, shadow_selector, options) {
		const element = await this.get(selector, options);
		return element.shadowRoot.querySelector(shadow_selector);
	}
	async getByText(selector, text) {
		const element = await this.get(selector);
		const children = element.children;
		return children.find(c => c.textContent === text) || children.find(async c => await this.getByText(c, text));
	}
	async getTextContent(selector) {
		const element = await this.get(selector);
		return element.textContent;
	}
	async getValue(selector) {
		const element = await this.get(selector);
		return element.value;
	}
	async getValueShadow(selector, shadow_selector) {
		const element = await this.getShadow(selector, shadow_selector);
		return element.value;
	}
	async getStyle(selector, style) {
		const element = await this.find(selector);
		return element.style[style];
	}
	async eval(selector, evaluator, options) {
		return evaluator.call(this.window, await this.get(selector, options));
	}
	async evalShadow(selector, shadow_selector, evaluator, options) {
		return evaluator.call(this.window, await this.getShadow(selector, shadow_selector), options);
	}
	async focus(selector) {
		const element = await this.get(selector);
		element.focus();
	}
	/**
	 * @param {string|HTMLElement} selector - The HTML element (or a string selector) to click
	 * @param {object} [options] - The options used to retrieve the HTML element is a selector is used
	 */
	async click(selector, options) {
		const element = await this.get(selector, options);
		element.focus();
		const click = new this.window.MouseEvent('click', mouse_event_properties(this.window, element));
		element.dispatchEvent(click);
	}
	async doubleClick(selector) {
		const element = await this.get(selector);
		const dblclick = new this.window.MouseEvent('dblclick', mouse_event_properties(this.window, element));
		element.dispatchEvent(dblclick);
	}
	async contextMenu(selector) {
		const element = await this.get(selector);
		const contextmenu = new this.window.MouseEvent('contextmenu', mouse_event_properties(this.window, element));
		element.dispatchEvent(contextmenu);
	}
	async dragAndDrop(draggable_selector, droppable_selector) {
		const draggable = await this.get(draggable_selector);
		const droppable = await this.get(droppable_selector);
		const data_transfer = new this.window.DataTransfer();

		const dragstart = new this.window.DragEvent('dragstart', {bubbles: true, cancelable: true, dataTransfer: data_transfer});
		draggable.dispatchEvent(dragstart);

		const dragenter = new this.window.DragEvent('dragenter', {bubbles: true, cancelable: true, dataTransfer: data_transfer});
		droppable.dispatchEvent(dragenter);

		const drop = new this.window.DragEvent('drop', {bubbles: true, cancelable: true, dataTransfer: data_transfer});
		droppable.dispatchEvent(drop);

		const dragend = new this.window.DragEvent('dragend', {bubbles: true, cancelable: true, dataTransfer: data_transfer});
		draggable.dispatchEvent(dragend);
	}
	//forms
	async type(selector, value) {
		const element = await this.get(selector);
		element.value = value;
		//trigger change event manually because it is not fired by the browser when the value is set with js
		this.#triggerChange(element);
		this.#triggerInput(element);
	}
	async check(selector) {
		const element = await this.get(selector);
		element.checked = true;
		this.#triggerChange(element);
	}
	async uncheck(selector) {
		const element = await this.get(selector);
		element.checked = false;
		this.#triggerChange(element);
	}
	async submit(selector) {
		const element = await this.get(selector);
		const submit = new this.window.SubmitEvent('submit', {bubbles: true, cancelable: true});
		element.dispatchEvent(submit);
		//submit event could throw an exception if form is not valid
	}
	//sequence must be an array of key like explained here https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key
	//that means you can send letters or digits directly such as "0", "4", "a" or "h" but not "Number0", "Number4", "KeyQ" or "KeyH"
	//you can also send other keys such as "Escape", "F2", "PageDown"
	//a valid sequence is ['a', 'Escape', 'q', '5', 'PageDown']
	//this code does not manage modifier keys (such as "Ctrl", "Alt" or "Shift") and only set the key property of the event (and not the code property)
	/**
	 * @param {Array<string>} sequence - An array of key to press (see https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key)
	 * @param {string|HTMLElement} [selector] - The HTML element (or a string selector) where the keys will be pressed
	 */
	async press(sequence, selector) {
		//keys are usually sent on the whole document element
		const element = selector ? await this.get(selector) : this.document;
		sequence.forEach(k => {
			this.#triggerKeydown(element, k);
			this.#triggerKeypress(element, k);
			this.#triggerKeyup(element, k);
		});
	}
	/**
	 * @param {number} [time] - Time to wait in milliseconds
	 * @returns {Promise} - A promise that resolves after the specified time
	 */
	async wait(time = 150) {
		return new Promise(resolve => {
			const timeout = time;
			this.window.setTimeout(resolve, timeout);
		});
	}
}
