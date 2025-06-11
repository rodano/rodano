import {Component, OnInit, Input, DestroyRef, OnChanges} from '@angular/core';
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
export class AutoCompleteComponent implements OnInit, OnChanges {
	@Input() field: CRFField;
	@Input() id: string;
	@Input() disabled: boolean;

	localizeMapPipe: LocalizeMapPipe;
	options: string[] = [];
	control = new FormControl('', {
		nonNullable: true
	});

	constructor(
		private fieldUpdateService: FieldUpdateService,
		private configurationService: ConfigurationService,
		private destroyRef: DestroyRef
	) {
		this.localizeMapPipe = new LocalizeMapPipe();
	}

	ngOnInit() {
		this.control.valueChanges.pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(value => {
			this.fieldUpdateService.updateField(this.field, value, value);
		});
		this.control.valueChanges.pipe(
			takeUntilDestroyed(this.destroyRef),
			switchMap(value => {
				if(!this.field.model.dictionary) {
					let possibleValues = this.field.model.possibleValues.map(p => this.localizeMapPipe.transform(p.shortname));
					if(value) {
						possibleValues = possibleValues.filter(p => p.toLowerCase().includes(value.toLowerCase()));
					}
					return of(possibleValues);
				}
				else {
					if(!value) {
						return of([]);
					}
					return this.configurationService.getAutocompleteOptions(this.field.model.datasetModelId, this.field.model.id, value);
				}
			})
		).subscribe(options => {
			//sort options
			options.sort((a, b) => a.localeCompare(b));
			this.options = options;
		});
	}

	ngOnChanges() {
		this.control.reset(this.field.value);
		if(this.field.model.readOnly || this.disabled) {
			this.control.disable();
		}
		if(!this.field.model.dictionary) {
			this.options = this.field.model.possibleValues.map(p => p.shortname['en']);
		}
	}
}
