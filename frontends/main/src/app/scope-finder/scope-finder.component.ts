import {Component, DestroyRef, ElementRef, HostBinding, Input, OnInit, Optional, Self, ViewChild} from '@angular/core';
import {ControlValueAccessor, NgControl, ReactiveFormsModule} from '@angular/forms';
import {MatOptgroup, MatOption} from '@angular/material/select';
import {LocalizeMapPipe} from '../pipes/localize-map.pipe';
import {MatFormFieldControl} from '@angular/material/form-field';
import {ConfigurationService} from '@core/services/configuration.service';
import {BooleanInput, coerceBooleanProperty} from '@angular/cdk/coercion';
import {ScopeModelDTO} from '@core/model/scope-model-dto';
import {ScopeCodeShortnamePipe} from '../pipes/scope-code-shortname.pipe';
import {MatInput} from '@angular/material/input';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {debounceTime, fromEvent, merge, Observable, of, switchMap} from 'rxjs';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ScopeService} from '@core/services/scope.service';
import {ScopeSearch} from '@core/utilities/search/scope-search';
import {ScopeDTO} from '@core/model/scope-dto';

@Component({
	selector: 'app-scope-finder',
	templateUrl: './scope-finder.component.html',
	styleUrls: ['./scope-finder.component.css'],
	imports: [
		ReactiveFormsModule,
		MatOptgroup,
		MatOption,
		MatInput,
		LocalizeMapPipe,
		ScopeCodeShortnamePipe,
		MatAutocompleteModule
	],
	providers: [
		//eslint-disable-next-line no-use-before-define
		{provide: MatFormFieldControl, useExisting: ScopeFinderComponent}
	]
})
export class ScopeFinderComponent implements MatFormFieldControl<number>, OnInit, ControlValueAccessor {
	static nextId = 0;

	@ViewChild('input', {read: MatInput, static: true}) input: MatInput;
	@ViewChild('input', {static: true}) inputElement: ElementRef<HTMLInputElement>;

	@HostBinding() id = `scope-finder-${ScopeFinderComponent.nextId++}`;
	controlType = 'scope-finder';

	onChange: (value: number | null) => void;

	scopePk: number | null = null;
	scopeModels: ScopeModelDTO[] = [];
	scopes: ScopeDTO[] = [];

	constructor(
		private configurationService: ConfigurationService,
		private scopeService: ScopeService,
		private destroyRef: DestroyRef,
		@Optional() @Self() public ngControl: NgControl
	) {
		if(this.ngControl !== null) {
			this.ngControl.valueAccessor = this;
		}
	}

	//forward component inputs to the underlying control
	//remember that the control may not have been initialized yet
	//keep track of the modification and apply them when the control is ready
	applyToUnderlyingControl(fn: (input: MatInput) => void) {
		if(this.input) {
			fn(this.input);
		}
		else {
			this.tasks.push(fn);
		}
	}

	tasks: ((input: MatInput) => void)[] = [];

	@Input()
	get value(): number | null {
		return this.scopePk;
	}

	set value(scopePk: number | null) {
		this.scopePk = scopePk;
		this.onChange(scopePk);
	}

	@Input()
	get disabled(): boolean {
		return this.input.disabled;
	}

	set disabled(value: boolean) {
		this.applyToUnderlyingControl(c => c.disabled = value);
	}

	@Input()
	get required(): boolean {
		return this.input.required;
	}

	set required(value: BooleanInput) {
		this.applyToUnderlyingControl(c => c.required = coerceBooleanProperty(value));
	}

	@Input()
	get placeholder() {
		return this.input.placeholder;
	}

	set placeholder(placeholder) {
		this.applyToUnderlyingControl(c => c.placeholder = placeholder);
	}

	//eslint-disable-next-line @angular-eslint/no-input-rename
	@Input('aria-describedby')
	set userAriaDescribedBy(value: string) {
		this.input.userAriaDescribedBy = value;
	}

	get userAriaDescribedBy(): string | undefined {
		return this.input.userAriaDescribedBy;
	}

	get shouldLabelFloat() {
		return this.input.shouldLabelFloat;
	}

	get stateChanges() {
		return this.input.stateChanges;
	}

	get empty(): boolean {
		return this.input.empty;
	}

	get focused(): boolean {
		return this.input.focused;
	}

	get errorState(): boolean {
		return this.input.errorState;
	}

	ngOnInit() {
		this.configurationService.getScopeModelsSorted().subscribe(s => this.scopeModels = s);

		//because the underlying control is injected with {static: true}, it is available in this hook method
		//no need to wait for afterViewInit
		setTimeout(() => {
			this.tasks.forEach(task => task(this.input));
			this.tasks = [];
		});

		const element = this.inputElement.nativeElement;
		merge(
			fromEvent(element, 'input'),
			fromEvent(element, 'focus')
		).pipe(
			takeUntilDestroyed(this.destroyRef),
			debounceTime(200),
			switchMap(() => {
				const value = element.value;
				if(!value) {
					return of(undefined);
				}
				const search = new ScopeSearch();
				search.fullText = value;
				return this.scopeService.search(search);
			}),
			switchMap(result => {
				return of(result?.objects ?? []);
			})
		).subscribe(scopes => {
			this.scopes = scopes;
		});
	}

	retrieveScope(scopePk: number | null): Observable<ScopeDTO | undefined> {
		if(!scopePk) {
			return of(undefined);
		}
		const scope = this.scopes.find(s => s.pk === scopePk);
		if(scope) {
			return of(scope);
		}
		return this.scopeService.get(scopePk);
	}

	getScopes(modelId: string): ScopeDTO[] {
		return this.scopes.filter(c => c.modelId === modelId) ?? [];
	}

	selectScope(scope: ScopeDTO) {
		this.value = scope.pk;
	}

	//this function cannot be a method because it is called statically bu the template
	displayScope = (scope: ScopeDTO) => {
		if(!scope) {
			//this is a workaround for the fact that there is no event when the input is reset because no option is selected
			this.value = null;
			return '';
		}
		return new ScopeCodeShortnamePipe().transform(scope);
	};

	setDescribedByIds(ids: string[]) {
		this.input.setDescribedByIds(ids);
	}

	onContainerClick() {
		this.input.onContainerClick();
	}

	writeValue(value: number | null) {
		this.retrieveScope(value).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(scope => {
			this.input.value = scope ? this.displayScope(scope) : '';
		});
	}

	registerOnChange(fn: any) {
		this.onChange = fn;
	}

	registerOnTouched(fn: any) {
		this.inputElement.nativeElement.addEventListener('blur', () => {
			fn();
		});
	}

	setDisabledState(isDisabled: boolean) {
		this.input.disabled = isDisabled;
	}
}
