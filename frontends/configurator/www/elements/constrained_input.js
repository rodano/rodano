class AppConstrainedInput extends HTMLElement {

	#internals;

	constructor() {
		super();
		this.#internals = this.attachInternals();

		const root = this.attachShadow({mode: 'open'});
		const template = /**@type {HTMLTemplateElement}*/ (document.getElementById('app-constrained-input'));
		root.appendChild(template.content.cloneNode(true));

		//retrieve datalist that will be used to offer suggestions
		this.datalist = root.querySelector('datalist');

		//retrieve input that will be used by user
		this.input = root.querySelector('input');
		//when the input is changed, set the value of the custom element to the value of the selected option
		this.input.addEventListener('change', () => {
			const input_value = this.input.value;
			const option = this.children.find(o => o.textContent === input_value);
			if(option) {
				this.value = option.getAttribute('value');
			}
			else {
				this.value = input_value;
			}
			//TODO move validation directly in the value setter when Firefox has been fixed (see value setter)
			this.validate();
		});

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

	#refresh() {
		//find the option matching the value
		let text = '';
		if(this.value) {
			const option = this.children.find(c => c.getAttribute('value') === this.value);
			if(option) {
				text = option.textContent;
			}
			else {
				text = this.value;
			}
		}
		this.input.value = text;
	}

	fill(entries) {
		//dot not mix options set for the custom elements of options of the embedded datalist
		//the options of the custom elements have a value and a label
		//the options of the embedded datalist can only have one value/label (that will be used to fill the input)
		entries
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
			this.input.setAttribute('disabled', 'disabled');
		}
		else {
			this.input.removeAttribute('disabled');
		}
	}

	formResetCallback() {
		this.value = '';
	}

	checkValidity() {
		return this.#internals.checkValidity();
	}

	reportValidity() {
		this.input.reportValidity();
		return this.#internals.reportValidity();
	}

	setCustomValidity(validity) {
		if(validity) {
			this.#internals.setValidity({customError: true}, validity);
			this.input.setCustomValidity(validity);
		}
		else {
			this.#internals.setValidity({});
			this.input.setCustomValidity('');
		}
		this.input.reportValidity();
	}

	validate() {
		this.#internals.setValidity({});
		this.input.setCustomValidity('');
		if(this.required && !this.value) {
			this.#internals.setValidity({valueMissing: true}, 'Field is required', this.input);
			this.input.setCustomValidity('Field is required');
		}
		if(this.value) {
			const option = this.children.find(o => o.value === this.value);
			if(!option) {
				this.#internals.setValidity({badInput: true}, 'Invalid value', this.input);
				this.input.setCustomValidity('Invalid value');
			}
		}
		this.reportValidity();
	}

	#handleOptions() {
		this.#addOptions(this.children);
	}

	#addOptions(options) {
		options.forEach(option => {
			//keep in mind that the value of the datalist's option is the label of the custom element's option
			if(!this.datalist.children.map(o => o.value).includes(option.textContent)) {
				//for datalist, only a value can be set for the option (no label)
				const datalist_option = option.cloneNode(true);
				datalist_option.value = datalist_option.textContent;
				datalist_option.textContent = '';
				this.datalist.appendChild(datalist_option);
			}
		});
	}

	#removeOptions(options) {
		options.forEach(option => {
			//keep in mind that the value of the datalist's option is the label of the custom element's option
			const existing_option = this.datalist.children.find(o => o.value === option.textContent);
			if(existing_option) {
				existing_option.remove();
			}
		});
	}

	connectedCallback() {
		const label_click = () => this.input.focus();

		//put focus on input element after a click on the associated labels
		//remember that labels are outside the shadow dom
		this.#internals.labels.forEach(l => l.addEventListener('click', label_click));

		//copy style from custom element to visible input
		this.input.style.width = document.defaultView.getComputedStyle(this, null).getPropertyValue('width');
		this.input.style.height = document.defaultView.getComputedStyle(this, null).getPropertyValue('height');

		this.#refresh();
	}
}

window.customElements.define('app-constrained-input', AppConstrainedInput);
