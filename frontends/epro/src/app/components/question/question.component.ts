import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { format, parse } from 'date-fns';
import { Scope } from 'src/app/api/model/scope-dto';
import { Event as EventDTO } from 'src/app/api/model/event-dto';
import { Field } from '../../api/model/field-dto';
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
export class QuestionComponent implements OnInit {

	@Input() rootScope: Scope;
	@Input() event: EventDTO;
	@Input() field: Field;
	@Input() allFields: Field[];

	// This is for the custom EQ5D questions
	@Input() isEQ5D: boolean;

	@Output() noAnswer = new EventEmitter<void>();

	selectedLanguage = 'en';
	maxDateValue?: string;

	ngOnInit() {
		if(!this.field.model.allowDateInFuture) {
			this.maxDateValue = new Date().toISOString();
		}
	}

	selectPossibleValue(value: string | undefined) {
		// TODO remove this as soon as the disabled feature is implemented in all ionic components
		if(!this.rootScope.locked && !this.event.locked) {
			this.field.value = value;
		}
	}

	private getDateFormat(): string {
		const model = this.field.model;
		let dateFormat = '';
		let timeFormat = '';

		if(model.withDays) {
			dateFormat += 'dd';
		}
		if(model.withMonths) {
			dateFormat += dateFormat ? '.MM' : 'MM';
		}
		if(model.withYears) {
			dateFormat += dateFormat ? '.yyyy' : 'yyyy';
		}

		if(model.withHours) {
			timeFormat += 'HH';
		}
		if(model.withMinutes) {
			timeFormat += timeFormat ? ':mm' : 'mm';
		}
		if(model.withSeconds) {
			timeFormat += timeFormat.includes(':') ? ':ss' : ':ss';
		}

		if(dateFormat && timeFormat) {
			return `${dateFormat} ${timeFormat}`;
		}

		return dateFormat || timeFormat || 'dd.MM.yyyy';
	}

	get readableDateTimeValue(): string | null {
		const dateFormat = this.getDateFormat();
		if(this.field.value && this.field.value !== '') {
			const parsedDate = parse(this.field.value, dateFormat, new Date());
			return format(parsedDate, dateFormat);
		}
		return null;
	}

	setDatetimeValue(newDateTime: string | string[] | null | undefined) {
		const dateFormat = this.getDateFormat();
		if(newDateTime && !Array.isArray(newDateTime)) {
			const dateTime = new Date(newDateTime);
			this.field.value = format(dateTime, dateFormat);
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

	onTouchMove(event: TouchEvent) {
		event.stopPropagation();
	}

	dateTimePickerPresentation(): string {
		const model = this.field.model;

		const hasDate = model.withYears || model.withMonths || model.withDays;
		const hasTime = model.withHours || model.withMinutes || model.withSeconds;

		if(hasDate && hasTime) {
			return 'date-time';
		} else if(hasDate) {
			return 'date';
		} else if(hasTime) {
			return 'time';
		} else {
			return 'date-time';
		}
	}

	maxDateTime(): string | undefined {
		return this.maxDateValue;
	}

	minDateTime(): string | undefined {
		const fieldId = this.field.model.id.toUpperCase();
		if(fieldId.includes('END')) {
			const beginTimeField = this.allFields?.find(f => f.model.id.toUpperCase().includes('BEGIN'));

			if(beginTimeField?.value) {
				const dateFormat = this.getDateFormat();
				const parsedDate = parse(beginTimeField.value, dateFormat, new Date());
				return parsedDate.toISOString();
			}
		}
		return undefined;
	}
}
