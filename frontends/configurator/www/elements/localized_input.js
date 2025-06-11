class AppLocalizedInput extends HTMLElement {

	#internals;

	constructor() {
		super();
		this.#internals = this.attachInternals();

		const root = this.attachShadow({mode: 'open'});
		const template = /**@type {HTMLTemplateElement}*/ (document.getElementById('app-localized-input'));
		root.appendChild(template.content.cloneNode(true));

		//create select that will be used to select field language
		this.select = /**@type {HTMLSelectElement}*/ (root.querySelector('select'));
		this.select.addEventListener('change', () => this.#refresh());

		//create field that will be used by user
		this.field = /**@type {HTMLInputElement | HTMLTextAreaElement}*/ (document.createFullElement(this.type || 'input'));
		this.field.addEventListener('input', () => {
			const values = this.getValues();
			values[this.select.value] = this.field.value;
			this.setValues(values);
			//TODO move validation directly in the value setter when Firefox has been fixed (see value setter)
			this.validate();
			const event = new UIEvent('change', {bubbles: true, cancelable: true});
			this.dispatchEvent(event);
		});
		this.shadowRoot.insertBefore(this.field, this.select);

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
	}

	static get observedAttributes() {
		return ['value'];
	}

	static formAssociated = true;

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

	set type(value) {
		if(value) {
			this.setAttribute('type', value);
		}
		else {
			this.removeAttribute('type');
		}
	}

	get type() {
		return this.getAttribute('type');
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

	get required() {
		return this.getAttribute('required');
	}

	set required(value) {
		if(value) {
			this.setAttribute('required', value);
		}
		else {
			this.removeAttribute('required');
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
			this.field.setAttribute('disabled', 'disabled');
			this.select.setAttribute('disabled', 'disabled');
		}
		else {
			this.field.removeAttribute('disabled');
			this.select.removeAttribute('disabled');
		}
	}

	formResetCallback() {
		this.value = '';
	}

	checkValidity() {
		return this.#internals.checkValidity();
	}

	reportValidity() {
		this.field.reportValidity();
		return this.#internals.reportValidity();
	}

	setCustomValidity(validity) {
		if(validity) {
			this.#internals.setValidity({customError: true}, validity, this.field);
			this.field.setCustomValidity(validity);
		}
		else {
			this.#internals.setValidity({});
			this.field.setCustomValidity('');
		}
		this.field.reportValidity();
	}

	validate() {
		const values = this.value ? JSON.parse(this.value) : {};
		if(this.required && Object.values(values).every(v => !v || !v.trim())) {
			this.#internals.setValidity({valueMissing: true}, 'Field is required', this.field);
			this.field.setCustomValidity('Field is required');
		}
		else {
			this.#internals.setValidity({});
			this.field.setCustomValidity('');
		}
		this.reportValidity();
	}

	getValues() {
		return this.value ? JSON.parse(this.value) : {};
	}

	setValues(values) {
		this.value = JSON.stringify(values);
	}

	#refresh() {
		this.field.value = this.getValues()[this.select.value] || '';
	}

	#handleOptions() {
		this.#addOptions(this.children);
	}

	#addOptions(options) {
		options.forEach(option => {
			if(!this.select.children.map(o => o.value).includes(option.value)) {
				this.select.appendChild(option.cloneNode(true));
				//select option if it is the first
				if(this.select.children.length === 1) {
					this.select.value = option.value;
					this.#refresh();
				}
			}
		});
	}

	#removeOptions(options) {
		//do not update selected values when options are removed
		//this may have unexpected behavior because mutation events are really asynchronous (for what it means)
		options.forEach(option => {
			const existing_option = this.select.children.find(o => o.value === option.value);
			if(existing_option) {
				existing_option.remove();
				//removing an option from the select may have changed its value (if the option that has been removed was the one selected)
				//native HTML handle this properly but the value displayed in the localized input must be updated
				this.#refresh();
			}
		});
	}

	connectedCallback() {
		const that = this;

		function label_click(event) {
			//remember that the label can be a separated element or a parent of the custom element
			//if it is a parent, a click anywhere in the custom element will trigger this event listener
			//this will not behave properly if the click happens in the select that allows to change the language
			//clicking this select (to open it to choose a language) will put the focus inside the input hence closing the select
			//that's why we need to check that the click didn't occur in the select
			//the problem is that the select is inside the shadow and the label is outside
			//so the event target only returns the custom element, not the select inside
			//fortunately, the "composePath" method allows to retrieve the path of the event even inside the shadow dom
			const path = event.composedPath();
			if(path[0] !== this.select) {
				that.field.focus();
			}
		}

		//put focus on input element after a click on the associated labels
		//remember that labels are outside the shadow dom
		this.#internals.labels.forEach(l => l.addEventListener('click', label_click));

		//copy style from custom element to visible input
		this.field.style.width = document.defaultView.getComputedStyle(this, null).getPropertyValue('width');
		this.field.style.height = document.defaultView.getComputedStyle(this, null).getPropertyValue('height');
	}
}

window.customElements.define('app-localized-input', AppLocalizedInput);
