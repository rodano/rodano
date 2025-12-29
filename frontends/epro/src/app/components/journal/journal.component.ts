import { Component, OnDestroy, OnInit } from '@angular/core';
import { Event } from '../../api/model/event-dto';
import { EventService } from '../../api/services/event.service';
import { filter, skip, switchMap, takeUntil, tap } from 'rxjs/operators';
import { forkJoin, Observable, of, Subject } from 'rxjs';
import { AlertController, IonicModule, RefresherCustomEvent, ToastController } from '@ionic/angular';
import { Scope } from 'src/app/api/model/scope-dto';
import { DatasetStateService } from 'src/app/services/dataset-state.service';
import { NavigationEnd, Router } from '@angular/router';
import { ScopeService } from 'src/app/api/services/scope.service';
import { ConfigurationService } from 'src/app/api/services/configuration.service';
import { EventModel } from 'src/app/api/model/event-model-dto';
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
	scope: Scope;
	events: Event[];
	availableEventModels: EventModel[];


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
		console.log('🔵 Journal ngOnInit');
		this.getUpdatedScopeAndEvents().pipe(
			tap(() => this.loading = true),
			switchMap(results => {
				const scope = results.scope;
				const events = results.events;
				console.log('🔵 Initial load - events:', events.length);
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
			console.log('🔵 Initial load complete - events:', this.events.length, 'models:', this.availableEventModels.length);
			this.loading = false;

			// TODO Same proble as in the surveys component, they should be merged
			this.router.events.pipe(
				tap(e => console.log('🟡 Router event:', e)),
				filter(e => e instanceof NavigationEnd && e.url === '/main/journal'),
				tap(() => console.log('🟢 Navigation to journal detected!')),
				skip(1),
				tap(() => {
					console.log('🟢 After skip(1) - will refresh');
					this.loading = true;
				}),
				switchMap(() => this.getUpdatedScopeAndEvents()),
				switchMap(updatedResults => {
					const events = updatedResults.events;
					console.log('🟢 Refresh - events:', events.length);
					const datasets$ = this.datasetStateService.pullDatasets(this.scope.pk, events.map(event => event.pk));
					const eventModels$ = this.scopeService.getAvailableEventModels(this.scope.pk);

					return forkJoin({
						events: of(events),
						datasets: datasets$,
						eventModels: eventModels$
					});
				}),
				takeUntil(this.unsubscribe$)
			).subscribe(refreshedResults => {
				console.log('🟢 Refresh complete - events:', refreshedResults.events.length, 'models:', refreshedResults.eventModels.length);
				this.events = refreshedResults.events;
				this.availableEventModels = refreshedResults.eventModels.sort((a, b) => {
					if(a.number && b.number) {
						return a.number - b.number;
					} else {
						return 1;
					}
				});
				this.loading = false;
			});
		});
	}

	createEvent(eventModel: EventModel) {
		console.log('🔴 Creating event:', eventModel.id);
		this.eventService.create(this.scope.pk, eventModel.eventModelId).pipe(
			switchMap(newEvent => {
				return forkJoin({
					newEvent: of(newEvent),
					datasets: this.datasetStateService.pullDatasets(this.scope.pk, [newEvent.pk]),
				});
			}),
			takeUntil(this.unsubscribe$)
		).subscribe({
			next: (results) => {
				console.log('🔴 Event created, navigating to survey');
				const newEvent = results.newEvent;
				const newDatasets = this.datasetStateService.getDatasetsForEvent(newEvent.pk);

				if(newDatasets && newDatasets.length > 0) {
					this.router.navigate([
						'/survey',
						newEvent.scopePk,
						newEvent.pk,
						newDatasets[0].pk
					]);
				}
				else {
					console.error('No dataset found for event: ', newEvent);
				}
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

	private getUpdatedScopeAndEvents(): Observable<{ scope: Scope, events: Event[]}> {
		return this.configService.getCurrentScope().pipe(
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
		this.getUpdatedScopeAndEvents().pipe(
			switchMap(updatedResults => {
				const events = updatedResults.events;
				const datasets$ = this.datasetStateService.pullDatasets(this.scope.pk, events.map(event => event.pk));

				return forkJoin({
					events: of(events),
					datasets: datasets$
				});
			})
		).subscribe(refreshedResults => {
			this.events = refreshedResults.events;
			$event.target.complete();
		});
	}

	public async onDelete(event: Event) {
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
