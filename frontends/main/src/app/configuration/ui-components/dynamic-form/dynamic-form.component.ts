import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatSlideToggle} from '@angular/material/slide-toggle';
import {MatInput} from '@angular/material/input';
import {MatOption, MatSelect} from '@angular/material/select';
import {FormsModule} from '@angular/forms';
import {MatIcon} from '@angular/material/icon';
import {DynamicArrayFieldComponent} from '../dynamic-array-field/dynamic-array-field.component';
import {getLabel, getSingularLabel, isNullableField, getObjectTemplate} from '../../fieldsMetadata';
import {MatButton} from '@angular/material/button';

@Component({
	selector: 'app-dynamic-form',
	imports: [
		MatLabel,
		MatSlideToggle,
		MatFormField,
		MatInput,
		MatSelect,
		MatOption,
		FormsModule,
		MatIcon,
		DynamicArrayFieldComponent,
		MatButton
	],
	templateUrl: './dynamic-form.component.html',
	styleUrl: './dynamic-form.component.css'
})
export class DynamicFormComponent implements OnInit, OnChanges {
	/**
		* The data object to generate the form from
		*/
	@Input() dto: Record<string, any>;

	/**
		* List of field names to exclude from the form
		*/
	@Input() excludedFields?: string[];

	/**
		* List of fields that should be immutable
		*/
	@Input() immutableFields?: string[];

	/**
		* List of field options (for dropdown rendering)
		*/
	@Input() fieldOptions?: Record<string, any[]>;

	/**
		* Used to apply indentation for nested object levels
		*/
	@Input() currentLayer = 0;

	/**
		* Callback fired when a field value changes
		*/
	@Input() onFieldChange?: (key: string, value: any, dto: Record<string, any>) => void;

	/**All top-level field keys in the dto, filtered via excludedFields**/
	fieldKeys: string[];

	/**Tracks collapsed state for nested objects**/
	collapsedFields: Record<string, boolean> = {};

	ngOnInit() {
		this.initializeFields();
		this.collapseNestedObjectArrays();
		console.log('fieldOptions: ', this.fieldOptions);
	}

	ngOnChanges(changes: SimpleChanges) {
		if(changes['excludedFields']) {
			this.filterFields();
		}
	}

	/**
		* Initializes the list of field keys by filtering out excluded fields
		*/
	private initializeFields(): void {
		if(this.dto) {
			this.fieldKeys = Object.keys(this.dto).filter(
				key => !this.excludedFields?.includes(key)
			);
		}
	}

	/**
	 * Collapses objects contained in array variables. Called when first loading a specific configuration to allow
	 * improved oversight for the user
	 * @private
	 */
	private collapseNestedObjectArrays() {
		for(const key of Object.keys(this.dto)) {
			if(this.isObjectArray(key, this.dto[key])) {
				this.dto[key].forEach((_item: any, index: number) => {
					const collapseKey = `${key}.${index}`;
					if(!(collapseKey in this.collapsedFields)) {
						this.collapsedFields[collapseKey] = true; //collapsed by default
					}
				});
			}
		}
	}

	/**
		* Determines the type of given field value.
		*/
	getFieldType(value: any): 'boolean' | 'string' | 'number' | 'unknown' {
		switch(typeof value) {
			case 'boolean':
				return 'boolean';
			case 'number':
				return 'number';
			case 'string':
				return 'string';
			default:
				return 'unknown';
		}
	}

	/**
		* Checks if the value is a non-array object (indicating nested data)
		*/
	hasNestedObject(value: any): boolean {
		return value && typeof value === 'object' && !Array.isArray(value);
	}

	/**
		* Checks if a given field has pre-defined options for a dropdown
		*/
	hasOptions(key: string): boolean {
		return this.fieldOptions ? key in this.fieldOptions : false;
	}

	/**
		* Toggles collapse state for a given key
		*/
	toggleCollapse(key: string): void {
		this.collapsedFields[key] = !this.collapsedFields[key];
	}

	/**
		* Returns whether a key is currently collapsed
		*/
	isCollapsed(key: string): boolean {
		return this.collapsedFields[key];
	}

	/**
		* Checks if a given value is an array
		*/
	isArray(value: any): boolean {
		return Array.isArray(value);
	}

	/**
		* Checks if a given value is an object array
		* @param key
		* @param value
		*/
	isObjectArray(key: string, value: any): boolean {
		if(Array.isArray(value)) {
			if(value.length === 0) {
				//Manually define known keys that should be treated as object arrays
				//I wasn't able to find a general solution that works when the field is empty
				return ['categories'].includes(key);
			}
			return typeof value[0] === 'object' && !Array.isArray(value[0]);
		}
		return false;
	}

	/**
		* Checks if a given value is a hex color string
		*/
	isHexColor(value: any): boolean {
		return typeof value === 'string' && /^#([0-9A-Fa-f]{3}){1,2}$/.test(value);
	}

	/**
		* Gets nested field options that are scoped under a specific parent key
		*/
	getNestedOptions(key: string): Record<string, any[]> {
		const prefix = `${key}.`;
		const nestedOptions: Record<string, any[]> = {};

		if(!this.fieldOptions) {
			return nestedOptions;
		}

		for(const optionKey in this.fieldOptions) {
			if(optionKey.startsWith(prefix)) {
				const newKey = optionKey.substring(prefix.length);
				nestedOptions[newKey] = this.fieldOptions[optionKey];
			}
		}

		return nestedOptions;
	}

	/**
		* Extracts nested excluded fields that are scoped under a parent key
		*/
	getNestedExcludedFields(parentKey: string): string[] {
		const nestedExcluded = this.excludedFields?.filter(f => f.startsWith(`${parentKey}.`)).map(f => f.slice(parentKey.length + 1));
		return nestedExcluded ?? [];
	}

	/**
		* Extracts nested immutable fields that are scoped under a parent key
		*/
	getNestedImmutableFields(parentKey: string): string[] {
		const nestedImmutable = this.immutableFields
			?.filter(f => f.startsWith(`${parentKey}.`))
			.map(f => f.slice(parentKey.length + 1));
		return nestedImmutable ?? [];
	}

	/**
		* Filters the fieldKeys array using the current excludedFields input
		*/
	filterFields() {
		if(this.fieldKeys) {
			this.fieldKeys = Object.keys(this.dto).filter(key => !this.excludedFields?.includes(key));
		}
	}

	/**
	 * Allows adding a new object to an object-array variable (E.g. adding a new category to a statistics chart)
	 * @param key
	 */
	addObjectToArray(key: string) {
		if(!Array.isArray(this.dto[key])) {
			this.dto[key] = [];
		}

		const template = getObjectTemplate(key);
		this.dto[key].push(structuredClone(template));

		this.onFieldChange?.(key, this.dto[key], this.dto);
	}

	/**
	 * Removes a specific object from an object-array variable (E.g. removing a category from a statistics chart)
	 * @param key
	 * @param index
	 */
	removeObjectFromArray(key: string, index: number): void {
		if(!Array.isArray(this.dto[key])) {
			return;
		}

		this.dto[key].splice(index, 1);

		this.onFieldChange?.(key, this.dto[key], this.dto);
	}

	/**
		* Checks if the given key is part of the immutable fields.
		*
		* @param {string} key - The key to check for immutability.
		* @return {boolean} Returns true if the key is listed as immutable, otherwise false.
		*/
	isImmutable(key: string): boolean {
		return this.immutableFields?.includes(key) ?? false;
	}

	//eslint-disable-next-line @typescript-eslint/no-unused-vars
	handleNestedFieldChange(parentKey: string, _nestedIndex: number) {
		//eslint-disable-next-line @typescript-eslint/no-unused-vars
		return (_nestedKey: string, _nestedValue: any, _nestedDto: Record<string, any>) => {
			this.onFieldChange?.(parentKey, this.dto[parentKey], this.dto);
		};
	}

	protected readonly getLabel = getLabel;
	protected readonly getSingularLabel = getSingularLabel;
	protected readonly isNullableField = isNullableField;
}
