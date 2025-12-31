import {Component, DestroyRef, Input, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink, RouterLinkActive} from '@angular/router';
import {EventService} from '@core/services/event.service';
import {MatDialog} from '@angular/material/dialog';
import {BehaviorSubject, combineLatest, forkJoin, Observable, of, switchMap} from 'rxjs';
import {FormService} from '@core/services/form.service';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatIcon} from '@angular/material/icon';
import {Scope} from '@core/model/scope';
import {Form} from '@core/model/form';
import {Event} from '@core/model/event';
import {MatButton, MatIconButton} from '@angular/material/button';
import {ScopeService} from '@core/services/scope.service';
import {SelectEventComponent} from '../dialogs/add-event/select-event.component';
import {CRFChangeService} from '../services/crf-change.service';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {EventGroup} from '@core/model/event-group';
import {NotificationService} from 'src/app/services/notification.service';
import {DateUTCPipe} from 'src/app/pipes/date-utc.pipe';
import {WorkflowableEntity} from '@core/model/workflowable-entity';
import {SettingsService} from '@core/services/settings.service';
import {PermissionsService} from '@core/services/permission.service';
import {AsyncPipe} from '@angular/common';

@Component({
	selector: 'app-side-menu',
	templateUrl: './side-menu.component.html',
	styleUrls: ['./side-menu.component.scss'],
	imports: [
		RouterLink,
		MatIcon,
		MatButton,
		MatIconButton,
		RouterLinkActive,
		MatTooltipModule,
		LocalizeMapPipe,
		DateUTCPipe,
		AsyncPipe
	]
})
export class SideMenuComponent implements OnInit {
	static EXPANDED_EVENT_PKS_PARAMETER = 'expandedEventPks';
	static EVENT_ORDERS_SETTING_KEY_PREFIX = 'eventOrders';

	static ALL_EVENT_GROUP = {
		eventGroupId: '00000000-0000-0000-0000-000000000001',
		id: 'ALL',
		shortname: {
			en: 'Events'
		}
	} satisfies EventGroup;

	static OTHER_EVENT_GROUP = {
		eventGroupId: '00000000-0000-0000-0000-000000000002',
		id: 'OTHER',
		shortname: {
			en: 'Other events'
		}
	} satisfies EventGroup;

	private scopeSubject$ = new BehaviorSubject<Scope | null>(null);
	public scope$ = this.scopeSubject$.asObservable();
	private _scope: Scope;

	@Input()
	set scope(value: Scope) {
		this._scope = value;
		if(value) {
			this.scopeSubject$.next(value);
		}
	}

	get scope(): Scope {
		return this._scope;
	}

	eventGroups: EventGroup[] = [];
	scopeForms: Form[];
	events: Event[];
	eventsForms: Record<number, Form[]> = {};

	eventOrdersByEventGroupId: Record<string, boolean> = {};

	expandedEventPks: number[] = [];
	eventPk: number | undefined = undefined;

	canWrite$: Observable<boolean>;

	constructor(
		private activatedRoute: ActivatedRoute,
		private scopeService: ScopeService,
		private eventService: EventService,
		private formService: FormService,
		private crfChangeService: CRFChangeService,
		private destroyRef: DestroyRef,
		private notificationService: NotificationService,
		private router: Router,
		private dialog: MatDialog,
		private settingsService: SettingsService,
		private permissionsService: PermissionsService
	) { }

	ngOnInit() {
		this.canWrite$ = this.permissionsService.canWrite();
		this.canWrite$.subscribe(canWrite => {
		});
		//do no try to be smart
		//when any workflowable is updated, refresh the whole menu
		//that's because rules may change workflow states on any other workflowable
		//it may also add/remove forms and events
		this.crfChangeService.updatedWorkflowable$.pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(typedWorkflowable => {
			if(typedWorkflowable.entity === WorkflowableEntity.SCOPE) {
				this.scope = typedWorkflowable.workflowable as Scope;
			}
		});

		combineLatest([
			this.scope$,
			this.activatedRoute.queryParams,
			//watch active route params to detect when parameters change, including child routes
			this.activatedRoute.params
		]).pipe(
			switchMap(([scope, queryParams]) => {
				return combineLatest([
					of(scope),
					scope !== null ? this.formService.searchOnScope(scope.pk) : of([]),
					scope !== null ? this.eventService.search(scope.pk) : of([]),
					of(queryParams)
				]);
			}),
			takeUntilDestroyed(this.destroyRef)
		).subscribe(([scope, forms, events, queryParams]) => {
			this.scopeForms = forms;
			this.events = events;
			if(scope !== null) {
				this.loadSortSettings();
				this.updateEventGroups();
				//retrieve and manage expanded event pks
				const expandedEventPks: string = queryParams[SideMenuComponent.EXPANDED_EVENT_PKS_PARAMETER] ?? '';
				this.expandedEventPks = expandedEventPks.split(',').filter(p => !!p).map(p => parseInt(p));
				//if the event is selected, open it
				//do not used the observed parameters, use the active route snapshot to access the child route parameters instead
				const params = this.activatedRoute.firstChild?.snapshot.params || {};
				this.eventPk = params['eventPk'] ? parseInt(params['eventPk']) : undefined;
				if(this.eventPk && !this.expandedEventPks.includes(this.eventPk)) {
					this.router.navigate([], {
						queryParams: {
							[SideMenuComponent.EXPANDED_EVENT_PKS_PARAMETER]: [...this.expandedEventPks, this.eventPk].join(',')
						},
						queryParamsHandling: 'merge'
					});
				}
				else {
					this.expandedEventPks.forEach(eventPk => {
						if(!this.eventsForms[eventPk]) {
							this.formService.searchOnEvent(scope.pk, eventPk).subscribe(forms => {
								this.eventsForms[eventPk] = forms;
							});
						}
					});
				}
			}
		});
	}

	updateEventGroups() {
		//build list of event groups, adding a placeholder event group if there are events without event group in the configuration
		if(this.scope.model.eventGroups.length === 0) {
			this.eventGroups = [SideMenuComponent.ALL_EVENT_GROUP];
		}
		else {
			this.eventGroups = [...this.scope.model.eventGroups];
			if(this.events.some(e => !e.model.eventGroupId)) {
				this.eventGroups.push(SideMenuComponent.OTHER_EVENT_GROUP);
			}
		}
	}

	getEvents(eventGroupId: string) {
		let events: Event[];
		if([SideMenuComponent.OTHER_EVENT_GROUP.eventGroupId, SideMenuComponent.ALL_EVENT_GROUP.eventGroupId].includes(eventGroupId)) {
			events = this.events.filter(e => !e.model.eventGroupId);
		}
		else {
			events = this.events.filter(e => e.model.eventGroupId === eventGroupId);
		}

		//sort events based on the sort order for this event group
		const isReverse = this.eventOrdersByEventGroupId[eventGroupId] || false;
		return [...events].sort((a, b) => {
			//sort events based on date if available, otherwise use the expected date
			const dateA = a.date ? new Date(a.date) : (a.expectedDate ? new Date(a.expectedDate) : new Date(0));
			const dateB = b.date ? new Date(b.date) : (b.expectedDate ? new Date(b.expectedDate) : new Date(0));

			const comparison = dateA.getTime() - dateB.getTime();
			return isReverse ? -comparison : comparison;
		});
	}

	toggleEventGroupSort(eventGroupId: string) {
		this.eventOrdersByEventGroupId[eventGroupId] = !this.eventOrdersByEventGroupId[eventGroupId];
		this.saveSortSettings();
	}

	isSortedChronologically(eventGroupId: string): boolean {
		return this.eventOrdersByEventGroupId[eventGroupId] || false;
	}

	private loadSortSettings() {
		this.eventOrdersByEventGroupId = this.settingsService.get(SideMenuComponent.EVENT_ORDERS_SETTING_KEY_PREFIX, {});
	}

	private saveSortSettings() {
		this.settingsService.set(SideMenuComponent.EVENT_ORDERS_SETTING_KEY_PREFIX, this.eventOrdersByEventGroupId);
	}

	generateToggleParameters(eventPk: number): Record<string, string> {
		//do not update expanded event pks here, it's used for the creation of a link to a new state
		const eventPks = this.expandedEventPks.includes(eventPk)
			? this.expandedEventPks.filter(pk => pk !== eventPk)
			: [...this.expandedEventPks, eventPk];
		if(eventPks.length === 0) {
			return {};
		}
		return {[SideMenuComponent.EXPANDED_EVENT_PKS_PARAMETER]: eventPks.join(',')};
	}

	createEvent() {
		this.scopeService.getAvailableEventModels(this.scope.pk).pipe(
			switchMap(events => {
				return this.dialog
					.open(SelectEventComponent, {data: events})
					.afterClosed();
			})
		).subscribe(eventModelId => {
			if(eventModelId) {
				this.eventService.create(this.scope.pk, eventModelId).pipe(
					switchMap(event => {
						return forkJoin({
							event: of(event),
							forms: this.formService.searchOnEvent(event.scopePk, event.pk)
						});
					})
				).subscribe({
					next: ({event, forms}) => {
						this.events.push(event);
						this.updateEventGroups();
						this.eventsForms[event.pk] = forms;
						this.router.navigate([
							'/crf',
							this.scope.pk,
							'event',
							event.pk,
							'form',
							forms[0].pk
						]);
					},
					error: error => {
						this.notificationService.showError(error.error.message);
					}
				});
			}
		});
	}
}
