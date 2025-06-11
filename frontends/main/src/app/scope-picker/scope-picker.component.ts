import {Component, HostBinding, Input, OnInit, Optional, Self, ViewChild} from '@angular/core';
import {ControlValueAccessor, NgControl, ReactiveFormsModule} from '@angular/forms';
import {MatOptgroup, MatOption, MatSelect} from '@angular/material/select';
import {LocalizeMapPipe} from '../pipes/localize-map.pipe';
import {ScopeMiniDTO} from '@core/model/scope-mini-dto';
import {MatFormFieldControl} from '@angular/material/form-field';
import {ConfigurationService} from '@core/services/configuration.service';
import {BooleanInput, coerceBooleanProperty} from '@angular/cdk/coercion';
import {ScopeModelDTO} from '@core/model/scope-model-dto';
import {ScopeCodeShortnamePipe} from '../pipes/scope-code-shortname.pipe';

@Component({
	selector: 'app-scope-picker',
	templateUrl: './scope-picker.component.html',
	styleUrls: ['./scope-picker.component.css'],
	imports: [
		ReactiveFormsModule,
		MatSelect,
		MatOptgroup,
		MatOption,
		LocalizeMapPipe,
		ScopeCodeShortnamePipe
	],
	providers: [
		//eslint-disable-next-line no-use-before-define
		{provide: MatFormFieldControl, useExisting: ScopePickerComponent}
	]
})
export class ScopePickerComponent implements MatFormFieldControl<number>, OnInit, ControlValueAccessor {
	static nextId = 0;

	@Input() scopes: ScopeMiniDTO[] = [];

	@ViewChild(MatSelect, {static: true}) select: MatSelect;

	@HostBinding() id = `scope-picker-${ScopePickerComponent.nextId++}`;
	controlType = 'scope-picker';

	scopeModels: ScopeModelDTO[] = [];

	constructor(
		private configurationService: ConfigurationService,
		@Optional() @Self() public ngControl: NgControl
	) {
		if(this.ngControl !== null) {
			this.ngControl.valueAccessor = this;
		}
	}

	//forward component inputs to the underlying control
	//remember that the control may not have been initialized yet
	//keep track of the modification and apply them when the control is ready
	applyToUnderlyingControl(fn: (select: MatSelect) => void) {
		if(this.select) {
			fn(this.select);
		}
		else {
			this.tasks.push(fn);
		}
	}

	tasks: ((select: MatSelect) => void)[] = [];

	@Input()
	get value(): number | null {
		return this.select.value;
	}

	set value(scopePk: number | null) {
		this.applyToUnderlyingControl(c => c.value = scopePk);
	}

	@Input()
	get disabled(): boolean {
		return this.select.disabled;
	}

	set disabled(value: boolean) {
		this.applyToUnderlyingControl(c => c.disabled = value);
	}

	@Input()
	get required(): boolean {
		return this.select.required;
	}

	set required(value: BooleanInput) {
		this.applyToUnderlyingControl(c => c.required = coerceBooleanProperty(value));
	}

	@Input()
	get placeholder() {
		return this.select.placeholder;
	}

	set placeholder(placeholder) {
		this.applyToUnderlyingControl(c => c.placeholder = placeholder);
	}

	//eslint-disable-next-line @angular-eslint/no-input-rename
	@Input('aria-describedby')
	set userAriaDescribedBy(value: string) {
		this.select.userAriaDescribedBy = value;
	}

	get userAriaDescribedBy(): string | undefined {
		return this.select.userAriaDescribedBy;
	}

	get disableAutomaticLabeling(): boolean | undefined {
		return this.select.disableAutomaticLabeling;
	}

	get shouldLabelFloat() {
		return this.select.shouldLabelFloat;
	}

	get stateChanges() {
		return this.select.stateChanges;
	}

	get empty(): boolean {
		return this.select.empty;
	}

	get focused(): boolean {
		return this.select.focused;
	}

	get errorState(): boolean {
		return this.select.errorState;
	}

	ngOnInit() {
		this.configurationService.getScopeModelsSorted().subscribe(s => this.scopeModels = s);

		//because the underlying control is injected with {static: true}, it is available in this hook method
		//no need to wait for afterViewInit
		setTimeout(() => {
			this.tasks.forEach(task => task(this.select));
			this.tasks = [];
		});
	}

	getScopes(modelId: string): ScopeMiniDTO[] {
		return this.scopes?.filter(c => c.modelId === modelId) ?? [];
	}

	setDescribedByIds(ids: string[]) {
		this.select.setDescribedByIds(ids);
	}

	onContainerClick() {
		this.select.onContainerClick();
	}

	writeValue(value: any) {
		this.select.writeValue(value);
	}

	registerOnChange(fn: any) {
		this.select.registerOnChange(fn);
	}

	registerOnTouched(fn: any) {
		this.select.registerOnTouched(fn);
	}

	setDisabledState(isDisabled: boolean) {
		this.select.setDisabledState(isDisabled);
	}

	onValueChange(value: any) {
		this.select.value = value;
	}
}
