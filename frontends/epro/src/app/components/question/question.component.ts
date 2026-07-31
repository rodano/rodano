import {Component, computed, inject, input, model, output} from '@angular/core';
import {Scope} from '@core/model/scope';
import {Event} from '@core/model/event';
import {Field} from '@core/model/field';
import {LocalizerPipe} from '../../pipes/localizer.pipe';
import {FormsModule} from '@angular/forms';
import {CdkTextareaAutosize} from '@angular/cdk/text-field';
import {MatButton} from '@angular/material/button';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatIcon} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatRadioModule} from '@angular/material/radio';
import {MatSliderModule} from '@angular/material/slider';
import {DateComponent} from '../date/date.component';
import {AppService} from '../../services/app.service';

@Component({
	selector: 'app-question',
	templateUrl: './question.component.html',
	styleUrls: ['./question.component.css'],
	imports: [
		MatButton,
		MatFormField,
		MatLabel,
		MatIcon,
		MatInputModule,
		MatRadioModule,
		MatSliderModule,
		CdkTextareaAutosize,
		DateComponent,
		FormsModule,
		LocalizerPipe
	]
})
export class QuestionComponent {
	private appService = inject(AppService);

	readonly rootScope = input.required<Scope>();
	readonly event = input.required<Event>();
	readonly field = input.required<Field>();

	//This is for the custom EQ5D questions
	readonly isEQ5D = input<boolean>(false);

	readonly value = model<string | undefined>();

	readonly noAnswer = output<void>();

	readonly selectedLanguage = this.appService.getSelectedLanguageId();

	readonly disabled = computed(() => this.rootScope().locked || this.event().locked);

	readonly numberValue = computed(() => {
		const value = this.value();
		return value === undefined || value === '' ? undefined : Number(value);
	});

	isFieldValueValid() {
		const value = this.numberValue();
		return value !== undefined && !isNaN(value);
	}

	setNumberValue(newValue: number) {
		this.value.set(String(newValue));
	}

	emitNoAnswer() {
		this.value.set('');
		this.noAnswer.emit();
	}
}
