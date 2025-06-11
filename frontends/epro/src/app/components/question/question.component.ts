import { Component, EventEmitter, Input, Output } from '@angular/core';
import { format, parse } from 'date-fns';
import { ScopeDTO } from 'src/app/api/model/scope-dto';
import { EventDTO } from 'src/app/api/model/event-dto';
import { FieldDTO } from '../../api/model/field-dto';
import { LocalizerPipe } from '../../pipes/localizer.pipe';
import { FormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';

@Component({
	selector: 'app-question',
	templateUrl: './question.component.html',
	styleUrls: ['./question.component.css'],
	standalone: true,
	imports: [
		IonicModule,
		FormsModule,
		LocalizerPipe
	]
})
export class QuestionComponent {

	@Input() rootScope: ScopeDTO;
	@Input() event: EventDTO;
	@Input() field: FieldDTO;

	// This is for the custom EQ5D questions
	@Input() isEQ5D: boolean;

	@Output() noAnswer = new EventEmitter<void>();

	selectedLanguage = 'en';

	selectPossibleValue(value: string | undefined) {
		// TODO remove this as soon as the disabled feature is implemented in all ionic components
		if(!this.rootScope.locked && !this.event.locked) {
			this.field.value = value;
		}
	}

	get readableDateTimeValue(): string | null {
		if(this.field.value && this.field.value !== '' && this.field.model.format) {
			const parsedDate = parse(this.field.value, this.field.model.format, new Date());
			return format(parsedDate, this.field.model.format);
		}
		return null;
	}

	setDatetimeValue(newDateTime: string | string[] | null | undefined) {
		if(newDateTime && !Array.isArray(newDateTime) && this.field.model.format) {
			const dateTime = new Date(newDateTime);
			const formattedDatetime = format(dateTime, this.field.model.format);
			this.field.value = formattedDatetime;
		} else {
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

	onTouchMove(event: Event) {
		event.stopPropagation();
	}

	dateTimePickerPresentation(): string {
		if(this.field.model.format) {
			if(this.field.model.format.includes('YYYY') && this.field.model.format.includes('HH')) {
				return 'date-time';
			} else if(this.field.model.format.includes('YYYY')) {
				return 'date';
			} else if(this.field.model.format.includes('HH')) {
				return 'time';
			} else {
				return 'date-time';
			}
		}
		throw new Error(`${this.field.model.id} does not have the date format`);
	}
}
