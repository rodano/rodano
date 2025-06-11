import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';
import { compareAsc, format } from 'date-fns';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { DatasetDTO } from 'src/app/api/model/dataset-dto';
import { ScopeDTO } from 'src/app/api/model/scope-dto';
import { EventDTO } from 'src/app/api/model/event-dto';
import { EventService } from 'src/app/api/services/event.service';
import { DatasetStateService } from 'src/app/services/dataset-state.service';
import { LocalizerPipe } from '../../pipes/localizer.pipe';
import { IonicModule } from '@ionic/angular';

@Component({
	selector: 'app-event-card',
	templateUrl: './event-card.component.html',
	styleUrls: ['./event-card.component.css'],
	standalone: true,
	imports: [IonicModule, LocalizerPipe],
})
export class EventCardComponent implements OnInit, OnDestroy {

	@Input() languageId: string;
	@Input() scope: ScopeDTO;
	@Input() event: EventDTO;

	@Output() deleted = new EventEmitter<EventDTO>();

	datasets: DatasetDTO[];

	unsubscribe$ = new Subject<void>();

	constructor(
		public datasetStateService: DatasetStateService,
		public eventService: EventService,
		private router: Router
	) { }

	ngOnInit() {
		const datasets$ = this.datasetStateService.getDatasetsForEvent$(this.event.pk);

		datasets$.pipe(
			takeUntil(this.unsubscribe$)
		).subscribe(eventDatasets => {
			this.datasets = eventDatasets;
		});
	}

	public getHumanReadableProgression(dataset: DatasetDTO): string {
		const progression = this.datasetStateService.getProgression(dataset);
		return `${Math.floor(progression * 100)}%`;
	}

	public getReadableDate(): string {
		const dateFormat = 'MMM d yyyy';

		const eventDate = this.event.date ? this.event.date : this.event.expectedDate;

		let resultString = format(eventDate, dateFormat);

		if(this.event.endDate) {
			resultString = resultString.concat(` - ${this.getEndDateString()}`);
		}

		return resultString;
	}

	private getEndDateString(): string {
		if(this.event.endDate === undefined) {
			throw new Error(`The end date has not been defined for the event ${this.event.pk}`);
		}

		const startDate = new Date(this.event.date);
		startDate.setHours(0, 0, 0, 0);

		const endDate = new Date(this.event.endDate);
		endDate.setHours(0, 0, 0, 0);

		let endDateFormat = '';
		if(compareAsc(startDate, endDate) === 0) {
			endDateFormat = 'HH:mm';
		} else {
			endDateFormat = 'MMM d yyyy - HH:mm';
		}

		return format(this.event.endDate, endDateFormat);
	}

	public onSelect(dataset: DatasetDTO) {
		this.router.navigate([
			'/survey',
			dataset.scopePk,
			dataset.eventPk,
			dataset.pk
		]);
	}

	public onDelete(event: EventDTO) {
		this.deleted.emit(event);
	}


	ngOnDestroy() {
		this.unsubscribe$.next();
		this.unsubscribe$.complete();
	}
}
