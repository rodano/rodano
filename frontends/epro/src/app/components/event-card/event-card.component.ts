import {Component, DestroyRef, OnInit, computed, inject, input, output, signal} from '@angular/core';
import {Router} from '@angular/router';
import {compareAsc, format} from 'date-fns';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {Dataset} from '@core/model/dataset';
import {Scope} from '@core/model/scope';
import {Event} from '@core/model/event';
import {EventService} from '@core/services/event.service';
import {DatasetStateService} from '../../services/dataset-state.service';
import {LocalizerPipe} from '../../pipes/localizer.pipe';
import {MatCardModule} from '@angular/material/card';
import {MatListModule} from '@angular/material/list';
import {MatIcon} from '@angular/material/icon';
import {MatButton} from '@angular/material/button';

@Component({
	selector: 'app-event-card',
	templateUrl: './event-card.component.html',
	styleUrls: ['./event-card.component.css'],
	imports: [MatCardModule, MatListModule, MatIcon, MatButton, LocalizerPipe]
})
export class EventCardComponent implements OnInit {
	readonly datasetStateService = inject(DatasetStateService);
	readonly eventService = inject(EventService);
	private router = inject(Router);
	private destroyRef = inject(DestroyRef);

	readonly scope = input.required<Scope>();
	readonly event = input.required<Event>();

	readonly deleted = output<Event>();

	readonly datasets = signal<Dataset[]>([]);

	readonly locked = computed(() => this.scope().locked || this.event().locked);
	readonly removable = computed(() => !this.eventService.isPlanned(this.event()));

	ngOnInit() {
		this.datasetStateService.getDatasetsForEvent$(this.event().pk).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(eventDatasets => this.datasets.set(eventDatasets));
	}

	getHumanReadableProgression(dataset: Dataset): string {
		const progression = this.datasetStateService.getProgression(dataset);
		return `${Math.floor(progression * 100)}%`;
	}

	getReadableDate(): string {
		const dateFormat = 'MMM d yyyy';

		const event = this.event();
		const eventDate = event.date ? event.date : event.expectedDate;

		let resultString = format(eventDate, dateFormat);

		if(event.endDate) {
			resultString = resultString.concat(` - ${this.getEndDateString()}`);
		}

		return resultString;
	}

	private getEndDateString(): string {
		const event = this.event();
		if(event.endDate === undefined) {
			throw new Error(`The end date has not been defined for the event ${event.pk}`);
		}

		const startDate = new Date(event.date);
		startDate.setHours(0, 0, 0, 0);

		const endDate = new Date(event.endDate);
		endDate.setHours(0, 0, 0, 0);

		const endDateFormat = compareAsc(startDate, endDate) === 0 ? 'HH:mm' : 'MMM d yyyy - HH:mm';

		return format(event.endDate, endDateFormat);
	}

	onSelect(dataset: Dataset) {
		this.router.navigate([
			'/survey',
			dataset.scopePk,
			dataset.eventPk,
			dataset.pk
		]);
	}

	onDelete() {
		this.deleted.emit(this.event());
	}
}
