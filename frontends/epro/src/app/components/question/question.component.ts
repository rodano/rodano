import {Component, EventEmitter, Input, Output, inject} from '@angular/core';
import {format, parse} from 'date-fns';
import {Scope} from '@core/model/scope';
import {Event} from '@core/model/event';
import {Field} from '@core/model/field';
import {LocalizerPipe} from '../../pipes/localizer.pipe';
import {FormsModule} from '@angular/forms';
import {IonButton, IonDatetime, IonIcon, IonInput, IonItem, IonLabel, IonList, IonPopover, IonRadioGroup, IonRange, IonRow, IonText, IonTextarea} from '@ionic/angular/standalone';
import {FieldService} from '@core/services/field.service';

@Component({
	selector: 'app-question',
	templateUrl: './question.component.html',
	styleUrls: ['./question.component.css'],
	standalone: true,
	imports: [
		IonButton,
		IonDatetime,
		IonIcon,
		IonInput,
		IonItem,
		IonLabel,
		IonList,
		IonPopover,
		IonRadioGroup,
		IonRange,
		IonRow,
		IonText,
		IonTextarea,
		FormsModule,
		LocalizerPipe
	]
})
export class QuestionComponent {
	@Input() rootScope: Scope;
	@Input() event: Event;
	@Input() field: Field;

	//This is for the custom EQ5D questions
	@Input() isEQ5D: boolean;

	@Output() noAnswer = new EventEmitter<void>();

	selectedLanguage = 'en';
	private fieldService = inject(FieldService);

	selectPossibleValue(value: string | undefined) {
		//TODO remove this as soon as the disabled feature is implemented in all ionic components
		if(!this.rootScope.locked && !this.event.locked) {
			this.field.value = value;
		}
	}

	get readableDateTimeValue(): string | null {
		if(this.field.value && this.field.value !== '') {
			const fmt = this.fieldService.generateFormat(this.field.model);
			const parsedDate = parse(this.field.value, fmt, new Date());
			return format(parsedDate, fmt);
		}
		return null;
	}

	setDatetimeValue(newDateTime: string | string[] | null | undefined) {
		if(newDateTime && !Array.isArray(newDateTime)) {
			const dateTime = new Date(newDateTime);
			const fmt = this.fieldService.generateFormat(this.field.model);
			const formattedDatetime = format(dateTime, fmt);
			this.field.value = formattedDatetime;
		}
		else {
			throw new Error('Can not accept multiple dates as input');
		}
	}

	isFieldValueValid() {
		const numberValue = Number(this.field.value);
		return this.field.value !== null && this.field.value !== undefined && !isNaN(numberValue);
	}

	emitNoAnswer() {
		this.field.value = '';
		this.noAnswer.emit();
	}

	onTouchMove(event: TouchEvent) {
		event.stopPropagation();
	}

	dateTimePickerPresentation(): string {
		if(this.fieldService.isDate(this.field.model) && this.fieldService.isTime(this.field.model)) {
			return 'date-time';
		}
		if(this.fieldService.isDate(this.field.model)) {
			return 'date';
		}
		if(this.fieldService.isTime(this.field.model)) {
			return 'time';
		}
		throw new Error(`${this.field.model.id} does not have the date format`);
	}
}
