import {Component, DestroyRef, OnInit, computed, inject, signal} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {combineLatest} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {MatToolbar} from '@angular/material/toolbar';
import {MatIcon} from '@angular/material/icon';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatProgressBar} from '@angular/material/progress-bar';
import {Dataset} from '@core/model/dataset';
import {Field} from '@core/model/field';
import {Event} from '@core/model/event';
import {Scope} from '@core/model/scope';
import {EventService} from '@core/services/event.service';
import {MeService} from '@core/services/me.service';
import {DatasetStateService} from '../../services/dataset-state.service';
import {NotificationService} from '../../services/notification.service';
import {LocalizerPipe} from '../../pipes/localizer.pipe';
import {QuestionComponent} from '../question/question.component';

@Component({
	templateUrl: './survey.component.html',
	styleUrls: ['./survey.component.css'],
	imports: [
		MatToolbar,
		MatIcon,
		MatButton,
		MatIconButton,
		MatProgressBar,
		QuestionComponent,
		LocalizerPipe
	]
})
export class SurveyComponent implements OnInit {
	private router = inject(Router);
	private activatedRoute = inject(ActivatedRoute);
	private eventService = inject(EventService);
	private datasetStateService = inject(DatasetStateService);
	private meService = inject(MeService);
	private notificationService = inject(NotificationService);
	private destroyRef = inject(DestroyRef);

	readonly rootScope = signal<Scope | undefined>(undefined);
	readonly event = signal<Event | undefined>(undefined);
	readonly dataset = signal<Dataset | undefined>(undefined);
	readonly datasetFields = signal<Field[]>([]);
	readonly fieldIndex = signal(0);

	//the value being edited is kept apart from the field so that changes made by the question component are tracked
	readonly value = signal<string | undefined>(undefined);

	readonly field = computed<Field | undefined>(() => this.datasetFields()[this.fieldIndex()]);
	readonly progress = computed(() => 100 * (this.fieldIndex() + 1) / this.datasetFields().length);
	readonly isFirstField = computed(() => this.fieldIndex() === 0);
	readonly isLastField = computed(() => this.fieldIndex() === this.datasetFields().length - 1);
	readonly previousLabel = computed(() => this.isFirstField() ? 'Back to surveys' : 'Previous question');
	readonly nextLabel = computed(() => this.isLastField() ? 'Finish' : 'Next question');

	private loaded = false;

	ngOnInit() {
		this.activatedRoute.params.pipe(
			switchMap(params => {
				const scopePk = parseInt(params['scopePk'], 10);
				const eventPk = parseInt(params['eventPk'], 10);
				const datasetPk = parseInt(params['datasetPk'], 10);

				return combineLatest([
					this.meService.getRootScope(),
					this.eventService.get(scopePk, eventPk),
					this.datasetStateService.pullDatasets(scopePk, [eventPk]).pipe(
						switchMap(() => this.datasetStateService.getDatasetForEvent$(eventPk, datasetPk))
					)
				]);
			}),
			takeUntilDestroyed(this.destroyRef)
		).subscribe(([rootScope, event, dataset]) => {
			this.rootScope.set(rootScope);
			this.event.set(event);
			this.dataset.set(dataset);

			//keep only the editable fields, in the order defined in the configuration
			this.datasetFields.set(
				dataset.fields
					.filter(f => !f.model.readOnly)
					.sort((field1, field2) => (field1.model.order ?? 0) - (field2.model.order ?? 0))
			);

			if(!this.loaded) {
				this.selectField(0);
				this.loaded = true;
			}
		});
	}

	next() {
		this.saveThen(() => {
			if(this.isLastField()) {
				this.notificationService.showSuccess('Thank you for answering the questions of this survey');
			}
			this.advanceToField(true);
		});
	}

	previous() {
		this.saveThen(() => this.advanceToField(false));
	}

	onNoAnswer() {
		this.next();
	}

	back() {
		this.navigateBack();
	}

	private saveThen(callback: () => void) {
		const rootScope = this.rootScope();
		const event = this.event();
		const dataset = this.dataset();
		const field = this.field();

		if(!rootScope || !event || !dataset || !field || rootScope.locked || event.locked) {
			callback();
			return;
		}

		field.value = this.value();
		this.datasetStateService.saveField(dataset, field).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(() => callback());
	}

	private advanceToField(forward: boolean) {
		if((!forward && this.isFirstField()) || (forward && this.isLastField())) {
			this.navigateBack();
		}
		else {
			this.selectField(this.fieldIndex() + (forward ? 1 : -1));
		}
	}

	private selectField(index: number) {
		this.fieldIndex.set(index);
		this.value.set(this.datasetFields()[index]?.value);
	}

	private navigateBack() {
		const event = this.event();
		if(event && this.eventService.isPlanned(event)) {
			this.router.navigate(['/main/surveys']);
		}
		else {
			this.router.navigate(['/main/journal']);
		}
	}
}
