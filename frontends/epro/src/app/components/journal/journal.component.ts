import {Component, DestroyRef, OnInit, inject, signal} from '@angular/core';
import {Event} from '@core/model/event';
import {EventService} from '@core/services/event.service';
import {filter, finalize, map, switchMap} from 'rxjs/operators';
import {Observable, forkJoin, of} from 'rxjs';
import {Scope} from '@core/model/scope';
import {DatasetStateService} from '../../services/dataset-state.service';
import {NavigationEnd, Router} from '@angular/router';
import {ScopeService} from '@core/services/scope.service';
import {MeService} from '@core/services/me.service';
import {EventModel} from '@core/model/event-model';
import {LocalizerPipe} from '../../pipes/localizer.pipe';
import {EventCardComponent} from '../event-card/event-card.component';
import {MatToolbar} from '@angular/material/toolbar';
import {MatIcon} from '@angular/material/icon';
import {MatFabButton, MatIconButton} from '@angular/material/button';
import {MatMenuModule} from '@angular/material/menu';
import {MatProgressBar} from '@angular/material/progress-bar';
import {MatDialog} from '@angular/material/dialog';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ConfirmDialogComponent, ConfirmDialogData} from '../../dialogs/confirm/confirm.dialog';
import {NotificationService} from '../../services/notification.service';

@Component({
	templateUrl: './journal.component.html',
	styleUrls: ['./journal.component.css'],
	imports: [
		MatToolbar,
		MatIcon,
		MatIconButton,
		MatFabButton,
		MatMenuModule,
		MatProgressBar,
		EventCardComponent,
		LocalizerPipe
	]
})
export class JournalComponent implements OnInit {
	private router = inject(Router);
	private meService = inject(MeService);
	private scopeService = inject(ScopeService);
	private eventService = inject(EventService);
	private datasetStateService = inject(DatasetStateService);
	private notificationService = inject(NotificationService);
	private dialog = inject(MatDialog);
	private destroyRef = inject(DestroyRef);

	readonly scope = signal<Scope | undefined>(undefined);
	readonly events = signal<Event[] | undefined>(undefined);
	readonly availableEventModels = signal<EventModel[]>([]);
	readonly loading = signal(false);

	ngOnInit() {
		this.refresh();

		//reload the events when navigating back to this page, as the dataset fields may have changed their properties (the event dates most notably)
		this.router.events.pipe(
			filter(e => e instanceof NavigationEnd && e.url === '/main/journal'),
			takeUntilDestroyed(this.destroyRef)
		).subscribe(() => this.refresh());
	}

	refresh() {
		this.loading.set(true);
		this.getScopeAndEvents().pipe(
			switchMap(({scope, events}) => forkJoin({
				scope: of(scope),
				events: of(events),
				datasets: this.datasetStateService.pullDatasets(scope.pk, events.map(e => e.pk)),
				eventModels: this.scopeService.getAvailableEventModels(scope.pk)
			})),
			finalize(() => this.loading.set(false)),
			takeUntilDestroyed(this.destroyRef)
		).subscribe(({scope, events, eventModels}) => {
			this.scope.set(scope);
			this.events.set(events);
			this.availableEventModels.set(
				[...eventModels].sort((a, b) => (a.number ?? Number.MAX_SAFE_INTEGER) - (b.number ?? Number.MAX_SAFE_INTEGER))
			);
		});
	}

	private getScopeAndEvents(): Observable<{scope: Scope; events: Event[]}> {
		return this.meService.getRootScope().pipe(
			switchMap(scope => this.eventService.search(scope.pk).pipe(
				map(events => ({scope, events: events.filter(e => !this.eventService.isPlanned(e))}))
			))
		);
	}

	createEvent(eventModel: EventModel) {
		const scope = this.scope();
		if(!scope) {
			return;
		}
		this.eventService.create(scope.pk, eventModel.id).pipe(
			//the datasets of the new event must be available before opening its survey
			switchMap(newEvent => this.datasetStateService.pullDatasets(newEvent.scopePk, [newEvent.pk]).pipe(
				map(() => newEvent)
			)),
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: newEvent => {
				const newDatasets = this.datasetStateService.getDatasetsForEvent(newEvent.pk);
				this.router.navigate([
					'/survey',
					newEvent.scopePk,
					newEvent.pk,
					newDatasets[0].pk
				]);
			},
			error: error => this.notificationService.showError(error.error.message)
		});
	}

	onDelete(event: Event) {
		this.dialog.open<ConfirmDialogComponent, ConfirmDialogData, boolean>(ConfirmDialogComponent, {
			data: {
				title: 'Delete event?',
				message: 'Are you sure you want to delete this event?',
				confirmLabel: 'Delete'
			}
		}).afterClosed().pipe(
			filter(confirmed => !!confirmed),
			switchMap(() => this.eventService.remove(event.scopePk, event.pk, 'Removed from ePro')),
			takeUntilDestroyed(this.destroyRef)
		).subscribe(() => {
			this.events.update(events => events?.filter(e => e.pk !== event.pk));
			this.notificationService.showSuccess('Event removed');
		});
	}
}
