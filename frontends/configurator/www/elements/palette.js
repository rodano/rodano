function list_dragleave() {
	this.classList.remove('highlight');
}

function item_dragstart(event) {
	this.style.opacity = 0.6;
	event.dataTransfer.effectAllowed = 'move';
	event.dataTransfer.setData('text/plain', this.title);
}

function item_dragleave() {
	this.classList.remove('highlight');
}

function item_dragend() {
	this.style.opacity = 1;
}

class AppPalette extends HTMLElement {

	#internals;

	constructor() {
		super();
		this.#internals = this.attachInternals();

		this.items = {};

		const that = this;

		function list_dragover(event) {
			if(that.#checkDropAllowed(this, event)) {
				event.preventDefault();
				event.dataTransfer.dropEffect = 'move';
				this.classList.add('highlight');
			}
		}

		function list_drop(event) {
			event.preventDefault();
			//update style whatever happens
			list_dragleave.call(this);
			if(that.#checkDropAllowed(this, event)) {
				const item = event.dataTransfer.getData('text/plain');
				//remove item from other list
				const other_list = this === that.selectedList ? that.availableList : that.selectedList;
				const item_ui = other_list.childNodes.find(c => c.title === item);
				//add it to this list (this will automatically remove it from the other list)
				this.appendChild(item_ui);
				//update model (based on ui)
				that.#saveValues();
			}
		}

		const root = this.attachShadow({mode: 'open'});
		const template = /**@type {HTMLTemplateElement}*/ (document.getElementById('app-palette'));
		root.appendChild(template.content.cloneNode(true));

		const available_div = root.children[1];
		const selected_div = root.children[2];

		this.availableList = available_div.querySelector('ul');
		this.availableList.addEventListener('dragenter', list_dragover);
		this.availableList.addEventListener('dragover', list_dragover);
		this.availableList.addEventListener('dragleave', list_dragleave);
		this.availableList.addEventListener('drop', list_drop);

		this.selectedList = selected_div.querySelector('ul');
		this.selectedList.addEventListener('dragenter', list_dragover);
		this.selectedList.addEventListener('dragover', list_dragover);
		this.selectedList.addEventListener('dragleave', list_dragleave);
		this.selectedList.addEventListener('drop', list_drop);

		const sort_image = available_div.querySelector('img');
		sort_image.addEventListener(
			'click',
			() => {
				if(!this.disabled) {
					const children = this.availableList.children.slice();
					let order;
					//first sort
					if(sort_image.style.opacity === '0.4') {
						sort_image.style.opacity = '1';
						order = 1;
					}
					else if(sort_image.src.includes('images/bullet_arrow_up.png')) {
						order = -1;
						sort_image.src = 'images/bullet_arrow_down.png';
					}
					else {
						order = 1;
						sort_image.src = 'images/bullet_arrow_up.png';
					}
					children.sort((c1, c2) => order * c1.textContent.compareTo(c2.textContent));
					children.forEach(Node.prototype.appendChild, this.availableList);
				}
			}
		);

		this.filter = available_div.querySelector('input');
		function update_filter() {
			that.availableList.children.forEach(c => c.style.display = c.textContent.nocaseIncludes(this.value) ? 'block' : 'none');
		}
		this.filter.addEventListener('input', update_filter);
		this.filter.addEventListener('search', update_filter);
	}

	static get observedAttributes() {
		return ['value'];
	}

	static formAssociated = true;

	#refresh() {
		const values = this.getValues();
		this.children.forEach(o => o.selected = values.includes(o.value));
		values.filter(v => this.items.hasOwnProperty(v)).forEach(v => this.selectedList.appendChild(this.items[v]));
	}

	attributeChangedCallback(name) {
		switch(name) {
			case 'value': {
				this.#refresh();
				break;
			}
		}
	}

	fill(entries) {
		entries
			.sort((e1, e2) => e1[1].compareTo(e2[1]))
			.map(e => document.createFullElement('option', {value: e[0]}, e[1]))
			.forEach(Node.prototype.appendChild, this.empty());
	}

	get name() {
		return this.getAttribute('name');
	}

	set name(value) {
		if(value) {
			this.setAttribute('name', value);
		}
		else {
			this.removeAttribute('name');
		}
	}

	get value() {
		return this.getAttribute('value');
	}

	set value(value) {
		if(value) {
			this.setAttribute('value', value);
		}
		else {
			this.removeAttribute('value');
		}
		this.#internals.setFormValue(value);
		const event = new UIEvent('change', {bubbles: true, cancelable: true});
		this.dispatchEvent(event);
	}

	get disabled() {
		return this.getAttribute('disabled');
	}

	set disabled(value) {
		if(value) {
			this.setAttribute('disabled', value);
		}
		else {
			this.removeAttribute('disabled');
		}
	}

	get form() {
		return this.#internals.form;
	}

	get validity() {
		return this.#internals.validity;
	}

	get validationMessage() {
		return this.#internals.validationMessage;
	}

	get willValidate() {
		return true;
	}

	//this is called when the disabled attribute is updated or when the disabled attribute of a fieldset containing this element is updated
	formDisabledCallback(disabled) {
		if(disabled) {
			this.filter.setAttribute('disabled', 'disabled');
			this.availableList.classList.add('disabled');
			this.selectedList.classList.add('disabled');
		}
		else {
			this.filter.removeAttribute('disabled');
			this.availableList.classList.remove('disabled');
			this.selectedList.classList.remove('disabled');
		}
	}

	formResetCallback() {
		this.value = '';
	}

	checkValidity() {
		return this.#internals.checkValidity();
	}

	reportValidity() {
		return this.#internals.reportValidity();
	}

	setCustomValidity(validity) {
		if(validity) {
			this.#internals.setValidity({customError: true}, validity);
		}
		else {
			this.#internals.setValidity({});
		}
	}

	getValues() {
		return this.value ? JSON.parse(this.value) : [];
	}

	setValues(values) {
		this.value = JSON.stringify(values);
	}

	#saveValues() {
		this.setValues(this.#getItemsFromList(this.selectedList));
	}

	#handleOptions() {
		this.#addOptions(this.children);
	}

	#addOptions(options) {
		const that = this;

		function item_dragover(event) {
			if(check_item_drop_allowed(this, event)) {
				event.stop();
				event.dataTransfer.dropEffect = 'move';
				this.classList.add('highlight');
			}
		}

		function item_drop(event) {
			event.preventDefault();
			//update style whatever happens
			item_dragleave.call(this);
			if(check_item_drop_allowed(this, event)) {
				const item = event.dataTransfer.getData('text/plain');
				//find item ui
				const list = this.parentNode;
				//look in this list
				let item_ui = list.childNodes.find(c => c.title === item);
				//look in the other list
				if(!item_ui) {
					const other_list = list === that.selectedList ? that.availableList : that.selectedList;
					item_ui = other_list.childNodes.find(c => c.title === item);
				}
				//insert new item ui (this will automatically remove it from the other list)
				list.insertBefore(item_ui, this);
				//update input (based on ui)
				that.#saveValues();
			}
		}

		function check_item_drop_allowed(item, event) {
			if(that.disabled) {
				return false;
			}
			const dragged_item = event.dataTransfer.getData('text/plain');
			return item.title !== dragged_item && that.#checkDropAllowed(item, event);
		}

		function transfer_item() {
			if(!that.disabled) {
				//update ui
				const new_parent_node = this.parentNode === that.availableList ? that.selectedList : that.availableList;
				this.parentNode.removeChild(this);
				new_parent_node.appendChild(this);
				//update model (based on ui)
				that.#saveValues();
			}
		}

		function draw_option(option) {
			const item_ui = document.createFullElement('li', {draggable: true, title: option.value}, option.textContent);
			item_ui.addEventListener('dblclick', transfer_item);
			item_ui.addEventListener('dragstart', item_dragstart);
			item_ui.addEventListener('dragend', item_dragend);
			item_ui.addEventListener('dragenter', item_dragover);
			item_ui.addEventListener('dragover', item_dragover);
			item_ui.addEventListener('dragleave', item_dragleave);
			item_ui.addEventListener('drop', item_drop);
			return item_ui;
		}

		options.forEach(option => {
			if(!this.items.hasOwnProperty(option.value)) {
				const item = draw_option(option);
				this.items[option.value] = item;
				this.availableList.appendChild(item);
			}
		});

		this.#refresh();
	}

	#removeOptions(options) {
		//do not update selected values when options are removed
		//this may have unexpected behavior because mutation events are really asynchronous
		options.forEach(option => {
			if(this.items.hasOwnProperty(option.value)) {
				this.items[option.value].remove();
				delete this.items[option.value];
			}
		});
	}

	#getItemsFromList(list) {
		return list.childNodes.map(c => c.title);
	}

	#checkDropAllowed(list, event) {
		if(this.disabled) {
			return false;
		}
		if(event.dataTransfer.types.includes('text/plain')) {
			//BUG some browsers do not allow to check what is dragged during drag
			if(event.dataTransfer.getData('text/plain')) {
				const item = event.dataTransfer.getData('text/plain');
				//check entries contains the item and that it is not already in the list
				return this.items.hasOwnProperty(item) && !this.#getItemsFromList(list).includes(item);
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

	connectedCallback() {
		const observer = new MutationObserver(list => {
			for(const mutation of list) {
				if(mutation.type === 'childList') {
					this.#addOptions(mutation.addedNodes);
					this.#removeOptions(mutation.removedNodes);
				}
			}
		});
		observer.observe(this, {childList: true});
		this.#handleOptions();
		this.#refresh();
	}
}

window.customElements.define('app-palette', AppPalette);
