import { Component, OnDestroy, OnInit } from '@angular/core';
import { EventDTO } from '../../api/model/event-dto';
import { EventService } from '../../api/services/event.service';
import { filter, first, switchMap, takeUntil, tap } from 'rxjs/operators';
import { forkJoin, Observable, of, Subject } from 'rxjs';
import { AlertController, RefresherCustomEvent, ToastController, IonicModule } from '@ionic/angular';
import { ScopeDTO } from 'src/app/api/model/scope-dto';
import { DatasetStateService } from 'src/app/services/dataset-state.service';
import { DatasetDTO } from 'src/app/api/model/dataset-dto';
import { NavigationEnd, Router } from '@angular/router';
import { ScopeService } from 'src/app/api/services/scope.service';
import { ConfigurationService } from 'src/app/api/services/configuration.service';
import { EventModelDTO } from 'src/app/api/model/event-model-dto';
import { LocalizerPipe } from '../../pipes/localizer.pipe';
import { EventCardComponent } from '../event-card/event-card.component';

@Component({
	templateUrl: './journal.component.html',
	styleUrls: ['./journal.component.css'],
	standalone: true,
	imports: [
		IonicModule,
		EventCardComponent,
		LocalizerPipe
	]
})
export class JournalComponent implements OnInit, OnDestroy {

	selectedLanguageId: string;
	scope: ScopeDTO;
	events: EventDTO[];
	availableEventModels: EventModelDTO[];

	refresh$: Observable<{ events: EventDTO[]; datasets: DatasetDTO[]; }>;

	loading = false;

	unsubscribe$ = new Subject<void>();

	constructor(
		private router: Router,
		private configService: ConfigurationService,
		private scopeService: ScopeService,
		private eventService: EventService,
		private datasetStateService: DatasetStateService,
		private alertCtrl: AlertController,
		private toastCtrl: ToastController
	) { }

	ngOnInit() {
		this.getUpdatedScopeAndEvents().pipe(
			tap(() => this.loading = true),
			switchMap(results => {
				const scope = results.scope;
				const events = results.events;
				const datasets$ = this.datasetStateService.pullDatasets(scope.pk, events.map(v => v.pk));

				return forkJoin({
					events: of(events),
					scope: of(scope),
					datasets: datasets$,
					eventModels: this.scopeService.getAvailableEventModels(scope.pk)
				});
			}),
			takeUntil(this.unsubscribe$)
		).subscribe(results => {
			this.events = results.events;
			this.scope = results.scope;
			this.availableEventModels = results.eventModels.sort((a, b) => {
				if(a.number && b.number) {
					return a.number - b.number;
				} else {
					return 1;
				}
			});

			this.loading = false;

			this.refresh$ = this.getUpdatedScopeAndEvents().pipe(
				switchMap(updatedResults => {
					const events = updatedResults.events;
					const datasets$ = this.datasetStateService.pullDatasets(this.scope.pk, events.map(event => event.pk));

					return forkJoin({
						events: of(events),
						datasets: datasets$
					});
				}),
				first()
			);

			// TODO Same proble as in the surveys component, they should be merged
			this.router.events.pipe(
				tap(() => this.loading = true),
				filter(e => e instanceof NavigationEnd && e.url === '/main/journal'),
				switchMap(() => this.refresh$),
				takeUntil(this.unsubscribe$)
			).subscribe(refreshedResults => {
				this.events = refreshedResults.events;

				this.loading = false;
			});
		});
	}

	createEvent(eventModel: EventModelDTO) {
		this.eventService.create(this.scope.pk, eventModel.id).pipe(
			switchMap(newEvent => {
				return forkJoin({
					newEvent: of(newEvent),
					refresh: this.refresh$
				});
			}),
			takeUntil(this.unsubscribe$)
		).subscribe({
			next: (results) => {
				this.events = results.refresh.events;

				const newEvent = results.newEvent;
				const newDatasets = this.datasetStateService.getDatasetsForEvent(newEvent.pk);

				this.router.navigate([
					'/survey',
					newEvent.scopePk,
					newEvent.pk,
					newDatasets[0].pk
				]);
			},
			error: async (error) => {
				console.log(error.error.message);

				const errToast = await this.toastCtrl.create({
					position: 'top',
					header: 'Error',
					message: error.error.message,
					color: 'danger',
					duration: 3000
				});

				errToast.present();
			}
		});
	}

	private getUpdatedScopeAndEvents(): Observable<{ scope: ScopeDTO, events: EventDTO[]}> {
		return this.configService.getRootScope().pipe(
			switchMap(scope => {
				return forkJoin({
					scope: of(scope),
					events: this.eventService.getForScope(scope.pk)
				});
			}),
			switchMap(updatedResults => {
				const events = updatedResults.events.filter(v => !this.eventService.isPlanned(v));
				return forkJoin({
					scope: of(updatedResults.scope),
					events: of(events)
				});
			}),
			takeUntil(this.unsubscribe$)
		);
	}

	onRefreshEvent($event: RefresherCustomEvent) {
		this.refresh$.subscribe(refreshedResults => {
			this.events = refreshedResults.events;
			$event.target.complete();
		});
	}

	public async onDelete(event: EventDTO) {
		const alert = await this.alertCtrl.create({
			header: 'Delete event?',
			message: 'Are you sure you want to delete this event?',
			buttons: [
				{
					text: 'Cancel',
					cssClass: 'secondary'
				},
				{
					text: 'Delete',
					cssClass: 'danger',
					handler: () => {
						this.eventService.remove(event.scopePk, event.pk, 'Removed from ePro').subscribe(async () => {
							const index = this.events.indexOf(event);
							this.events.splice(index, 1);

							const confirmToast = await this.toastCtrl.create({
								position: 'top',
								message: 'Event removed',
								duration: 2000
							});
							confirmToast.present();
						});
					}
				}
			]
		});
		await alert.present();
	}


	ngOnDestroy() {
		this.unsubscribe$.next();
		this.unsubscribe$.complete();
	}
}
