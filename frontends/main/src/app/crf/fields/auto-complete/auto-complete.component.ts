import {ChangeDetectionStrategy, Component, OnInit, DestroyRef, effect, input, signal} from '@angular/core';
import {ReactiveFormsModule, FormControl} from '@angular/forms';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatInput} from '@angular/material/input';
import {MatFormField} from '@angular/material/form-field';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {CRFField} from '../../models/crf-field';
import {FieldUpdateService} from '../../services/field-update.service';
import {ConfigurationService} from '@core/services/configuration.service';
import {of, switchMap} from 'rxjs';
import {LocalizeMapPipe} from 'src/app/pipes/localize-map.pipe';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	selector: 'app-auto-complete',
	templateUrl: './auto-complete.component.html',
	styleUrls: ['../field/field.component.css', './auto-complete.component.css'],
	imports: [
		MatFormField,
		MatInput,
		MatAutocompleteModule,
		ReactiveFormsModule
	]
})
export class AutoCompleteComponent implements OnInit {
	readonly field = input.required<CRFField>();
	readonly id = input.required<string>();
	readonly disabled = input<boolean>(false);

	localizeMapPipe: LocalizeMapPipe;
	readonly options = signal<string[]>([]);
	control = new FormControl('', {
		nonNullable: true
	});

	constructor(
		private fieldUpdateService: FieldUpdateService,
		private configurationService: ConfigurationService,
		private destroyRef: DestroyRef
	) {
		this.localizeMapPipe = new LocalizeMapPipe();
		effect(() => {
			const field = this.field();
			this.control.reset(field.value);
			if(field.model.readOnly || this.disabled()) {
				this.control.disable();
			}
			if(!field.model.dictionary) {
				this.options.set(field.model.possibleValues.map(p => p.shortname['en']));
			}
		});
	}

	ngOnInit() {
		this.control.valueChanges.pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(value => {
			this.fieldUpdateService.updateField(this.field(), value, value);
		});
		this.control.valueChanges.pipe(
			takeUntilDestroyed(this.destroyRef),
			switchMap(value => {
				const field = this.field();
				if(!field.model.dictionary) {
					let possibleValues = field.model.possibleValues.map(p => this.localizeMapPipe.transform(p.shortname));
					if(value) {
						possibleValues = possibleValues.filter(p => p.toLowerCase().includes(value.toLowerCase()));
					}
					return of(possibleValues);
				}
				else {
					if(!value) {
						return of([]);
					}
					return this.configurationService.getAutocompleteOptions(field.model.datasetModelId, field.model.id, value);
				}
			})
		).subscribe(options => {
			//sort options
			options.sort((a, b) => a.localeCompare(b));
			this.options.set(options);
		});
	}
}
