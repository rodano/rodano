import {Component, OnDestroy, OnInit} from '@angular/core';
import {Event} from '@core/model/event';
import {EventService} from '@core/services/event.service';
import {filter, first, switchMap, takeUntil, tap} from 'rxjs/operators';
import {forkJoin, Observable, of, Subject} from 'rxjs';
import {AlertController, IonContent, IonHeader, IonProgressBar, IonRefresher, IonRefresherContent, IonText, IonTitle, IonToolbar, RefresherCustomEvent, ToastController} from '@ionic/angular/standalone';
import {Scope} from '@core/model/scope';
import {DatasetStateService} from '../../services/dataset-state.service';
import {Dataset} from '@core/model/dataset';
import {NavigationEnd, Router} from '@angular/router';
import {MeService} from '@core/services/me.service';
import {EventCardComponent} from '../event-card/event-card.component';

@Component({
	templateUrl: './surveys.component.html',
	styleUrls: ['./surveys.component.css'],
	standalone: true,
	imports: [
		IonContent,
		IonHeader,
		IonProgressBar,
		IonRefresher,
		IonRefresherContent,
		IonText,
		IonTitle,
		IonToolbar,
		EventCardComponent
	]
})
export class SurveysComponent implements OnInit, OnDestroy {
	selectedLanguageId: string;
	scope: Scope;
	events: Event[];

	refresh$: Observable<{events: Event[]; datasets: Dataset[]}>;

	loading = false;

	unsubscribe$ = new Subject<void>();

	constructor(
		private router: Router,
		private meService: MeService,
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
					datasets: datasets$
				});
			}),
			takeUntil(this.unsubscribe$)
		).subscribe(results => {
			this.events = results.events;
			this.scope = results.scope;

			this.loading = false;

			//This is used to reload the events when we redirect back to this component since their properties
			//can be changed by the dataset fields (the event dates most notably)
			//TODO There must be a better way to do this
			this.router.events.pipe(
				tap(() => this.loading = true),
				filter(e => e instanceof NavigationEnd && e.url === '/main/surveys'),
				switchMap(() => this.refresh$),
				takeUntil(this.unsubscribe$)
			).subscribe(refreshedResults => {
				this.events = refreshedResults.events;

				this.loading = false;
			});
		});

		this.refresh$ = this.getUpdatedScopeAndEvents().pipe(
			switchMap(updatedResults => {
				const events = updatedResults.events;
				const datasets$ = this.datasetStateService.pullDatasets(this.scope.pk, events.map(v => v.pk));

				return forkJoin({
					events: of(events),
					datasets: datasets$
				});
			}),
			first()
		);
	}

	private getUpdatedScopeAndEvents(): Observable<{scope: Scope; events: Event[]}> {
		return this.meService.getRootScope().pipe(
			switchMap(scope => {
				return forkJoin({
					scope: of(scope),
					events: this.eventService.search(scope.pk)
				});
			}),
			switchMap(results => {
				const filteredEvents = results.events.filter(event => this.eventService.isEventPlannedAndDue(event));

				return forkJoin({
					scope: of(results.scope),
					events: of(filteredEvents)
				});
			}),
			takeUntil(this.unsubscribe$)
		);
	}

	public onRefresh($event: RefresherCustomEvent) {
		this.refresh$.subscribe(refreshedResults => {
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
