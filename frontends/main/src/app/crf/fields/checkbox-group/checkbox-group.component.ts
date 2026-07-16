import {Component, DestroyRef, effect, input, signal} from '@angular/core';
import {ReactiveFormsModule, FormGroup, FormControl} from '@angular/forms';
import {LocalizeMapPipe} from '../../../pipes/localize-map.pipe';
import {MatInput} from '@angular/material/input';
import {MatFormField} from '@angular/material/form-field';
import {MatCheckbox} from '@angular/material/checkbox';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {CRFField} from '../../models/crf-field';
import {FieldUpdateService} from '../../services/field-update.service';

@Component({
	selector: 'app-checkbox-group',
	templateUrl: './checkbox-group.component.html',
	styleUrls: ['../field/field.component.css', './checkbox-group.component.css'],
	imports: [
		ReactiveFormsModule,
		MatCheckbox,
		MatFormField,
		MatInput,
		LocalizeMapPipe
	]
})
export class CheckboxGroupComponent {
	readonly field = input.required<CRFField>();
	readonly disabled = input<boolean>(false);

	//this cannot be static as it is used in the template
	readonly otherControlName = 'OTHER_SPECIFY_VALUE';

	specifyValueId = signal<string | undefined>(undefined);
	formGroup: FormGroup;

	constructor(
		private fieldUpdateService: FieldUpdateService,
		private destroyRef: DestroyRef
	) {
		effect(() => {
			const field = this.field();

			//retrieve the possible value that asks for a specify value if any
			const specifyValueId = field.model.possibleValues.find(v => v.specify)?.id;
			this.specifyValueId.set(specifyValueId);

			//get the values of the field, and the "other" value if any
			const fieldValues = field.value?.split(',') ?? [];
			const otherValue = fieldValues.find(v => !field.model.possibleValues.some(pv => pv.id === v));

			const controls = {} as Record<string, FormControl<string | boolean | null>>;
			field.model.possibleValues.forEach(value => {
				if(value.specify) {
					controls[value.id] = new FormControl<boolean>(!!otherValue);
					controls[this.otherControlName] = new FormControl<string>(otherValue as string);
				}
				else {
					controls[value.id] = new FormControl<boolean>(fieldValues.includes(value.id));
				}
			});
			this.formGroup = new FormGroup(controls);

			this.formGroup.valueChanges.pipe(
				takeUntilDestroyed(this.destroyRef)
			).subscribe((values: Record<string, boolean>) => {
				const field = this.field();

				//retrieve all selected values except the one that asks for an other value
				const fieldValues = field.model.possibleValues.map(v => v.id).filter(v => this.specifyValueId() !== v && values[v]);
				if(specifyValueId && values[specifyValueId]) {
					fieldValues.push(this.formGroup.get(this.otherControlName)?.value as string);
				}
				const fieldValue = fieldValues.join(',');
				const fieldValueLabel = fieldValues.map(value => {
					const possibleValue = field.model.possibleValues.find(v => v.id === value);
					return possibleValue ? new LocalizeMapPipe().transform(possibleValue.shortname) : value;
				}).join(', ');
				this.fieldUpdateService.updateField(field, fieldValue, fieldValueLabel);
			});

			if(field.model.readOnly || this.disabled()) {
				this.formGroup.disable();
			}
			if(this.field().model.readOnly || this.disabled()) {
				this.formGroup.disable();
			}
			else {
				this.formGroup.enable();
			}
		});
	}
}
