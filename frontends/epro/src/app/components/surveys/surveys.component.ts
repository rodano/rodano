import {Component, DestroyRef, OnInit, inject, signal} from '@angular/core';
import {Event} from '@core/model/event';
import {EventService} from '@core/services/event.service';
import {filter, finalize, map, switchMap} from 'rxjs/operators';
import {Observable} from 'rxjs';
import {Scope} from '@core/model/scope';
import {DatasetStateService} from '../../services/dataset-state.service';
import {NavigationEnd, Router} from '@angular/router';
import {MeService} from '@core/services/me.service';
import {EventCardComponent} from '../event-card/event-card.component';
import {MatToolbar} from '@angular/material/toolbar';
import {MatIcon} from '@angular/material/icon';
import {MatIconButton} from '@angular/material/button';
import {MatProgressBar} from '@angular/material/progress-bar';
import {MatDialog} from '@angular/material/dialog';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ConfirmDialogComponent, ConfirmDialogData} from '../../dialogs/confirm/confirm.dialog';
import {NotificationService} from '../../services/notification.service';

@Component({
	templateUrl: './surveys.component.html',
	styleUrls: ['./surveys.component.css'],
	imports: [
		MatToolbar,
		MatIcon,
		MatIconButton,
		MatProgressBar,
		EventCardComponent
	]
})
export class SurveysComponent implements OnInit {
	private router = inject(Router);
	private meService = inject(MeService);
	private eventService = inject(EventService);
	private datasetStateService = inject(DatasetStateService);
	private notificationService = inject(NotificationService);
	private dialog = inject(MatDialog);
	private destroyRef = inject(DestroyRef);

	readonly scope = signal<Scope | undefined>(undefined);
	readonly events = signal<Event[] | undefined>(undefined);
	readonly loading = signal(false);

	ngOnInit() {
		this.refresh();

		//reload the events when navigating back to this page, as the dataset fields may have changed their properties (the event dates most notably)
		this.router.events.pipe(
			filter(e => e instanceof NavigationEnd && e.url === '/main/surveys'),
			takeUntilDestroyed(this.destroyRef)
		).subscribe(() => this.refresh());
	}

	refresh() {
		this.loading.set(true);
		this.getScopeAndEvents().pipe(
			switchMap(({scope, events}) => this.datasetStateService.pullDatasets(scope.pk, events.map(e => e.pk)).pipe(
				map(() => ({scope, events}))
			)),
			finalize(() => this.loading.set(false)),
			takeUntilDestroyed(this.destroyRef)
		).subscribe(({scope, events}) => {
			this.scope.set(scope);
			this.events.set(events);
		});
	}

	private getScopeAndEvents(): Observable<{scope: Scope; events: Event[]}> {
		return this.meService.getRootScope().pipe(
			switchMap(scope => this.eventService.search(scope.pk).pipe(
				map(events => ({scope, events: events.filter(e => this.eventService.isEventPlannedAndDue(e))}))
			))
		);
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
