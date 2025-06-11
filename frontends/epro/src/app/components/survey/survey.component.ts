import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastController, IonicModule } from '@ionic/angular';
import { DatasetDTO } from '../../api/model/dataset-dto';
import { FieldDTO } from '../../api/model/field-dto';
import { switchMap, takeUntil } from 'rxjs/operators';
import { combineLatest, Subject } from 'rxjs';
import { DatasetStateService } from 'src/app/services/dataset-state.service';
import { EventDTO } from 'src/app/api/model/event-dto';
import { EventService } from 'src/app/api/services/event.service';
import { ScopeDTO } from 'src/app/api/model/scope-dto';
import { compareAsc } from 'date-fns';
import { ConfigurationService } from 'src/app/api/services/configuration.service';
import { LocalizerPipe } from '../../pipes/localizer.pipe';
import { QuestionComponent } from '../question/question.component';

@Component({
	templateUrl: './survey.component.html',
	styleUrls: ['./survey.component.css'],
	standalone: true,
	imports: [
		IonicModule,
		QuestionComponent,
		LocalizerPipe
	]
})
export class SurveyComponent implements OnInit, OnDestroy {

	rootScope: ScopeDTO;
	event: EventDTO;
	dataset: DatasetDTO;
	datasetFields: FieldDTO[];
	field: FieldDTO;

	loaded = false;

	selectedLanguage = 'en';

	unsubscribe$ = new Subject<void>();

	constructor(
		private router: Router,
		private activatedRoute: ActivatedRoute,
		private toastCtrl: ToastController,
		private eventService: EventService,
		private datasetStateService: DatasetStateService,
		private configService: ConfigurationService
	) {	}

	ngOnInit() {
		this.activatedRoute.params.pipe(
			switchMap(params => {
				const scopePk = parseInt(params.scopePk, 10);
				const eventPk = parseInt(params.eventPk, 10);
				const datasetPk = parseInt(params.datasetPk, 10);

				return combineLatest([
					this.configService.getRootScope(),
					this.eventService.get(scopePk, eventPk),
					this.datasetStateService.pullDatasets(scopePk, [eventPk]).pipe(
						switchMap(() => this.datasetStateService.getDatasetForEvent$(eventPk, datasetPk))
					)
				]);
			}),
			takeUntil(this.unsubscribe$)
		).subscribe(results => {
			this.rootScope = results[0];
			this.event = results[1];
			this.dataset = results[2];

			// Get all the fields from the dataset and filter out the readonly fields
			this.datasetFields = this.dataset.fields
				.filter(f => !f.model.readOnly)
				.sort((field1, field2) => {
					if(field1.model.order && field2.model.order) {
						return field1.model.order - field2.model.order;
					} else {
						return 0;
					}
				});

			if(!this.loaded) {
				this.field = this.datasetFields[0];
				this.loaded = true;
			}
		});
	}

	getFieldIndex(): number {
		return this.datasetFields.findIndex(field => field.modelId === this.field.modelId);
	}

	isFirstField(): boolean {
		return this.getFieldIndex() === 0;
	}

	isLastField(): boolean {
		return this.getFieldIndex() === this.datasetFields.length - 1;
	}

	getPreviousLabel(): string {
		return this.isFirstField() ? 'Back to surveys' : 'Previous question';
	}

	getNextLabel(): string {
		return this.isLastField() ? 'Finish' : 'Next question';
	}

	nextFieldModel() {
		// save the dataset if the rootscope and the event are not locked
		if(!this.rootScope.locked && !this.event.locked) {
			this.datasetStateService.saveField(this.dataset, this.field).pipe(
				takeUntil(this.unsubscribe$)
			).subscribe(() => {
				if(this.isLastField()) {
					this.toastSuccess();
				}

				this.advanceToField(true);
			});
		} else {
			this.advanceToField(true);
		}
	}

	previousFieldModel() {
		// save the dataset if the rootscope and the event are not locked
		if(!this.rootScope.locked && !this.event.locked && this.field.value !== null) {
			this.datasetStateService.saveField(this.dataset, this.field).pipe(
				takeUntil(this.unsubscribe$)
			).subscribe(() => {
				this.advanceToField(false);
			});
		} else {
			this.advanceToField(false);
		}
	}

	getEndDateFormat(): string {
		if(this.event.endDate === undefined) {
			throw new Error(`The end date has not been defined for the event ${this.event.pk}`);
		}

		const startDate = new Date(this.event.date);
		startDate.setHours(0, 0, 0, 0);

		const endDate = new Date(this.event.endDate);
		endDate.setHours(0, 0, 0, 0);

		if(compareAsc(startDate, endDate) === 0) {
			return 'HH:mm';
		} else {
			return 'MMM d yyyy - HH:mm';
		}
	}

	private advanceToField(forward: boolean) {
		if((!forward && this.isFirstField()) || (forward && this.isLastField())) {
			this.navigateBack();
		}
		else {
			const index = this.getFieldIndex();

			let nextIndex;
			if(forward) {
				nextIndex = index + 1;
			} else {
				nextIndex = index - 1;
			}

			const nextField = this.datasetFields[nextIndex];
			this.field = this.datasetFields.find(f => f.modelId === nextField.modelId) as FieldDTO;
		}
	}

	private navigateBack() {
		if(this.eventService.isPlanned(this.event)) {
			this.router.navigate(['/main/surveys']);
		} else {
			this.router.navigate(['/main/journal']);
		}
	}

	async toastSuccess() {
		const toast = await this.toastCtrl.create({
			position: 'top',
			message: 'Thank you for answering the questions of this survey',
			duration: 3000,
		});
		toast.present();
	}

	onNoAnswer() {
		this.nextFieldModel();
	}


	ngOnDestroy() {
		this.unsubscribe$.next();
		this.unsubscribe$.complete();
	}
}
