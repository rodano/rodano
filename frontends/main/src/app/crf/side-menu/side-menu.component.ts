import {Component, DestroyRef, ElementRef, Injector, OnInit, afterNextRender, effect, model, signal} from '@angular/core';
import {ActivatedRoute, Router, RouterLink, RouterLinkActive} from '@angular/router';
import {EventService} from '@core/services/event.service';
import {MatDialog} from '@angular/material/dialog';
import {EMPTY, combineLatest, forkJoin, map, of, switchMap} from 'rxjs';
import {FormService} from '@core/services/form.service';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {MatIcon} from '@angular/material/icon';
import {Scope} from '@core/model/scope';
import {Form} from '@core/model/form';
import {Event} from '@core/model/event';
import {MatButton, MatIconButton} from '@angular/material/button';
import {ScopeService} from '@core/services/scope.service';
import {SelectEventComponent} from '../dialogs/add-event/select-event.component';
import {WorkflowableUpdateService} from '../services/workflowable-update.service';
import {takeUntilDestroyed, toObservable} from '@angular/core/rxjs-interop';
import {EventGroup} from '@core/model/event-group';
import {NotificationService} from '../../services/notification.service';
import {DateUTCPipe} from '../../pipes/date-utc.pipe';
import {WorkflowableEntity} from '@core/model/workflowable-entity';
import {SettingsService} from '@core/services/settings.service';
import {MatTooltip} from '@angular/material/tooltip';

@Component({
	selector: 'app-side-menu',
	templateUrl: './side-menu.component.html',
	styleUrl: './side-menu.component.scss',
	imports: [
		RouterLink,
		MatIcon,
		MatTooltip,
		MatButton,
		MatIconButton,
		RouterLinkActive,
		LocalizeMapPipe,
		DateUTCPipe
	]
})
export class SideMenuComponent implements OnInit {
	static EXPANDED_EVENT_PKS_PARAMETER = 'expandedEventPks';
	static EVENT_ORDERS_SETTING_KEY_PREFIX = 'eventOrders';

	static ALL_EVENT_GROUP = {
		id: 'ALL',
		shortname: {
			en: 'Events'
		}
	} satisfies EventGroup;

	static OTHER_EVENT_GROUP = {
		id: 'OTHER',
		shortname: {
			en: 'Other events'
		}
	} satisfies EventGroup;

	readonly scope = model.required<Scope>();
	readonly scope$ = toObservable(this.scope);

	readonly eventGroups = signal<EventGroup[]>([]);
	readonly scopeForms = signal<Form[]>([]);
	events: Event[];
	readonly eventsForms = signal<Record<number, Form[]>>({});

	eventOrdersByEventGroupId: Record<string, boolean> = {};

	readonly expandedEventPks = signal<number[]>([]);
	readonly eventPk = signal<number | undefined>(undefined);

	constructor(
		private activatedRoute: ActivatedRoute,
		private scopeService: ScopeService,
		private eventService: EventService,
		private formService: FormService,
		private workflowableUpdateService: WorkflowableUpdateService,
		private destroyRef: DestroyRef,
		private notificationService: NotificationService,
		private router: Router,
		private dialog: MatDialog,
		private settingsService: SettingsService,
		private elementRef: ElementRef<HTMLElement>,
		private injector: Injector
	) {
		//scroll the currently expanded event into view, whether it was just created or opened through a direct link
		effect(() => {
			const eventPk = this.eventPk();
			if(!eventPk) {
				return;
			}
			//also depend on the event's forms: the submenu is empty until they are loaded
			const forms = this.eventsForms()[eventPk];
			if(!forms) {
				return;
			}
			//wait for the forms to be rendered in the menu before scrolling to them
			afterNextRender(() => {
				this.elementRef.nativeElement.querySelector(`#event-${eventPk}-forms`)?.scrollIntoView({block: 'end'});
			}, {injector: this.injector});
		});
	}

	ngOnInit() {
		//do no try to be smart
		//when any workflowable is updated, refresh the whole menu
		//that's because rules may change workflow states on any other workflowable
		//it may also add/remove forms and events
		this.workflowableUpdateService.updatedWorkflowable$.pipe(
			switchMap(typedWorkflowable => {
				//if the workflowable is a scope, use it as-is
				if(typedWorkflowable.entity === WorkflowableEntity.SCOPE) {
					return of(typedWorkflowable.workflowable as Scope);
				}
				//if not, refetch it to get the latest status
				return this.scopeService.get(this.scope().pk);
			}),
			takeUntilDestroyed(this.destroyRef)
		).subscribe(scope => {
			//clean-up event forms cache
			this.eventsForms.set({});
			//setting the scope will trigger a menu refresh through the scope observable
			this.scope.set(scope);
		});

		this.scope$.pipe(
			//fetch forms and events only when the scope changes
			switchMap(scope => forkJoin([
				this.formService.searchOnScope(scope.pk),
				this.eventService.search(scope.pk)
			]).pipe(
				//then keep listening to route param changes without re-fetching
				switchMap(([forms, events]) => combineLatest([
					this.activatedRoute.queryParams,
					//watch active route params to detect when parameters change, including child routes
					this.activatedRoute.params
				]).pipe(
					map(([queryParams]) => ({scope, forms, events, queryParams}))
				))
			)),
			takeUntilDestroyed(this.destroyRef)
		).subscribe(({scope, forms, events, queryParams}) => {
			this.scopeForms.set(forms);
			this.events = events;
			this.loadSortSettings();
			this.updateEventGroups();
			//retrieve and manage expanded event pks
			const expandedEventParameter: string = queryParams[SideMenuComponent.EXPANDED_EVENT_PKS_PARAMETER] ?? '';
			const newExpandedEventPks = expandedEventParameter.split(',').filter(p => !!p).map(p => parseInt(p));
			this.expandedEventPks.set(newExpandedEventPks);
			//do not used the observed parameters, use the active route snapshot to access the child route parameters instead
			const params = this.activatedRoute.firstChild?.snapshot?.params || {};
			const newEventPk = params['eventPk'] ? parseInt(params['eventPk']) : undefined;
			this.eventPk.set(newEventPk);
			//load forms for every expanded event
			const formsRequests = Object.fromEntries(this.expandedEventPks()
				.filter(eventPk => !this.eventsForms()[eventPk])
				.map(eventPk => [eventPk, this.formService.searchOnEvent(scope.pk, eventPk)]));
			if(Object.keys(formsRequests).length > 0) {
				forkJoin(formsRequests)
					.pipe(takeUntilDestroyed(this.destroyRef))
					.subscribe(eventForms => {
						this.eventsForms.update(ef => ({...ef, ...eventForms}));
					});
			}
		});
	}

	updateEventGroups() {
		//build list of event groups, adding a placeholder event group if there are events without event group in the configuration
		if(this.scope().model.eventGroups.length === 0) {
			this.eventGroups.set([SideMenuComponent.ALL_EVENT_GROUP]);
		}
		else {
			const eventGroups = [...this.scope().model.eventGroups];
			if(this.events.some(e => !e.model.eventGroupId)) {
				eventGroups.push(SideMenuComponent.OTHER_EVENT_GROUP);
			}
			this.eventGroups.set(eventGroups);
		}
	}

	getEvents(eventGroupId: string) {
		let events: Event[];
		if([SideMenuComponent.OTHER_EVENT_GROUP.id, SideMenuComponent.ALL_EVENT_GROUP.id].includes(eventGroupId)) {
			events = this.events.filter(e => !e.model.eventGroupId);
		}
		else {
			events = this.events.filter(e => e.model.eventGroupId === eventGroupId);
		}

		//sort events based on the sort order for this event group
		const isReverse = this.eventOrdersByEventGroupId[eventGroupId] || false;
		return [...events].sort((a, b) => {
			//sort events based on date if available, otherwise use the expected date
			const dateA = new Date(a.date ?? a.expectedDate ?? 0);
			const dateB = new Date(b.date ?? b.expectedDate ?? 0);

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

	generateToggleParameters(eventPk: number): Record<string, string | null> {
		//do not update expanded event pks here, it's used for the creation of a link to a new state
		//allow to open only one event at a time for now
		return this.expandedEventPks().includes(eventPk)
			? {[SideMenuComponent.EXPANDED_EVENT_PKS_PARAMETER]: null}
			: {[SideMenuComponent.EXPANDED_EVENT_PKS_PARAMETER]: eventPk.toString()};
		//allowing multiple open events is disabled for now
		/*const eventPks = this.expandedEventPks().includes(eventPk)
			? this.expandedEventPks().filter(pk => pk !== eventPk)
			: [...this.expandedEventPks(), eventPk];
		if(eventPks.length === 0) {
			return {};
		}
		return {[SideMenuComponent.EXPANDED_EVENT_PKS_PARAMETER]: eventPks.join(',')};*/
	}

	createEvent() {
		this.scopeService.getAvailableEventModels(this.scope().pk).pipe(
			switchMap(events => this.dialog.open(SelectEventComponent, {data: events}).afterClosed()),
			switchMap(eventModelId => {
				if(!eventModelId) {
					return EMPTY;
				}
				return this.eventService.create(this.scope().pk, eventModelId).pipe(
					switchMap(event => forkJoin({
						event: of(event),
						forms: this.formService.searchOnEvent(event.scopePk, event.pk)
					}))
				);
			})
		).subscribe({
			next: ({event, forms}) => {
				this.events.push(event);
				this.updateEventGroups();
				this.eventsForms.update(ef => ({...ef, [event.pk]: forms}));
				this.router.navigate(['/crf', this.scope().pk, 'events', event.pk, 'forms', forms[0].pk], {
					queryParams: {[SideMenuComponent.EXPANDED_EVENT_PKS_PARAMETER]: event.pk.toString()}
				});
			},
			error: error => {
				this.notificationService.showError(error.error.message);
			}
		});
	}
}
