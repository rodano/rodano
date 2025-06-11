import {Component, ElementRef, HostListener, Input, OnInit} from '@angular/core';
import {MatFormField} from '@angular/material/form-field';
import {MatIcon} from '@angular/material/icon';
import {MatInput} from '@angular/material/input';
import {CdkOverlayOrigin, OverlayModule} from '@angular/cdk/overlay';
import {getLabel} from '../../fieldsMetadata';
import {MatOption, MatSelect} from '@angular/material/select';
import {FormControl, ReactiveFormsModule} from '@angular/forms';

@Component({
	selector: 'app-dynamic-array-field',
	imports: [
		ReactiveFormsModule,
		MatFormField,
		MatIcon,
		MatInput,
		OverlayModule,
		MatSelect,
		MatOption
	],
	templateUrl: './dynamic-array-field.component.html',
	styleUrl: './dynamic-array-field.component.css'
})
export class DynamicArrayFieldComponent implements OnInit {
	/**
		* The data transfer object that holds the array field being edited.
		*/
	@Input() dto: Record<string, any>;

	/**
		* Used to apply indentation for nested object levels
		*/
	@Input() currentLayer = 0;

	/**
		* The key within `dto` that references the array.
		*/
	@Input() key: string;

	/**
		* Optional list of selectable options (e.g., for dropdown/autocomplete).
		*/
	@Input() options: any[] = [];

	/**
		* Callback function triggered whenever the field's value changes.
		*/
	@Input() onFieldChange?: (key: string, value: any, dto: any) => void;

	constructor(private elRef: ElementRef) {}

	/**Index of the currently edited array item */
	activeIndex: number | null = null;

	/**Variable which holds the current user input **/
	editedValue = new FormControl('');

	/**Flag to know if we are adding or editing an item **/
	isAddingNew = false;

	/**Reference to the overlay trigger element **/
	activeOverlayOrigin: CdkOverlayOrigin | null = null;

	arrayControl = new FormControl([]);

	ngOnInit() {
		if(Array.isArray(this.dto[this.key])) {
			this.arrayControl.setValue(this.dto[this.key]);
		}

		this.arrayControl.valueChanges.subscribe(value => {
			this.dto[this.key] = value;
			this.onFieldChange?.(this.key, value, this.dto);
		});
	}

	/**
		* Opens the overlay for editing an existing array item.
		* @param index Index of the item in the array.
		* @param origin The overlay origin (used to anchor the overlay).
		*/
	openOverlay(index: number, origin: CdkOverlayOrigin): void {
		this.activeIndex = index;
		this.activeOverlayOrigin = origin;
		this.editedValue.setValue(this.dto[this.key][index]);
	}

	/**
		* Opens the overlay for adding a new array item.
		* @param origin The overlay origin (used to anchor the overlay).
		*/
	openAddOverlay(origin: CdkOverlayOrigin): void {
		this.isAddingNew = true;
		this.activeIndex = null;
		this.activeOverlayOrigin = origin;
		this.editedValue.reset(''); //When we are adding, we start with an empty value
	}

	/**
		* Closes any open overlay.
		*/
	closeOverlay(): void {
		this.activeIndex = null;
		this.activeOverlayOrigin = null;
		this.isAddingNew = false;
	}

	/**
		* Adds a new item to the array at the given key.
		* @param key Key referencing the array in the DTO.
		*/
	addItem(key: string): void {
		const value = this.editedValue.value?.trim();
		if(value) {
			this.dto[key].push(value);
			this.onFieldChange?.(key, this.dto[key], this.dto);
			this.editedValue.reset('');
		}
	}

	/**
		* Removes an item from the array.
		* @param key Key referencing the array in the DTO.
		* @param idx Index of the item to remove.
		*/
	removeItem(key: string, idx: number): void {
		this.dto[key].splice(idx, 1);
		if(this.activeIndex === idx) {
			this.closeOverlay();
		}
	}

	/**
		* Applies changes from the overlay to either add or update an item.
		*/
	applyChange(): void {
		if(this.isAddingNew) {
			this.addItem(this.key);
		}
		else if(this.activeIndex !== null) {
			this.dto[this.key][this.activeIndex] = this.editedValue;
			this.onFieldChange?.(this.key, this.dto[this.key], this.dto);
		}
		this.closeOverlay();
	}

	/**
		* Checks whether a value is a hex color string.
		*/
	isHexColor(value: any): boolean {
		return typeof value === 'string' && /^#([0-9A-Fa-f]{3}){1,2}$/.test(value);
	}

	/**
		* Closes the overlay if a click occurs outside the component.
		*/
	@HostListener('document:click', ['$event'])
	onDocumentClick(event: Event) {
		if(!this.elRef.nativeElement.contains(event.target)) {
			this.closeOverlay();
		}
	}

	/**
		* Stops click propagation inside the overlay to prevent it from triggering close.
		*/
	onOverlayClick(event: MouseEvent) {
		event.stopPropagation();
	}

	protected readonly getLabel = getLabel;
}
