import {Component, DestroyRef, Input, OnChanges, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink, RouterLinkActive} from '@angular/router';
import {EventService} from '@core/services/event.service';
import {MatDialog} from '@angular/material/dialog';
import {combineLatest, forkJoin, of, switchMap} from 'rxjs';
import {FormService} from '@core/services/form.service';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatIcon} from '@angular/material/icon';
import {ScopeDTO} from '@core/model/scope-dto';
import {FormDTO} from '@core/model/form-dto';
import {EventDTO} from '@core/model/event-dto';
import {MatButton} from '@angular/material/button';
import {ScopeService} from '@core/services/scope.service';
import {SelectEventComponent} from '../dialogs/add-event/select-event.component';
import {CRFChangeService} from '../services/crf-change.service';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {EventGroupDTO} from '@core/model/event-group-dto';
import {NotificationService} from 'src/app/services/notification.service';
import {DateUTCPipe} from 'src/app/pipes/date-utc.pipe';
import {WorkflowableEntity} from '@core/model/workflowable-entity';

@Component({
	selector: 'app-side-menu',
	templateUrl: './side-menu.component.html',
	styleUrls: ['./side-menu.component.scss'],
	imports: [
		RouterLink,
		MatIcon,
		MatButton,
		RouterLinkActive,
		MatTooltipModule,
		LocalizeMapPipe,
		DateUTCPipe
	]
})
export class SideMenuComponent implements OnInit, OnChanges {
	static EXPANDED_EVENT_PKS_PARAMETER = 'expandedEventPks';

	static ALL_EVENT_GROUP = {
		id: 'ALL',
		shortname: {
			en: 'Events'
		}
	} satisfies EventGroupDTO;

	static OTHER_EVENT_GROUP = {
		id: 'OTHER',
		shortname: {
			en: 'Other events'
		}
	} satisfies EventGroupDTO;

	@Input() scope: ScopeDTO;
	eventGroups: EventGroupDTO[] = [];
	scopeForms: FormDTO[];
	events: EventDTO[];
	eventsForms: Record<number, FormDTO[]> = {};
	expandedEventPks: number[] = [];

	constructor(
		private activatedRoute: ActivatedRoute,
		private scopeService: ScopeService,
		private eventService: EventService,
		private formService: FormService,
		private crfChangeService: CRFChangeService,
		private destroyRef: DestroyRef,
		private notificationService: NotificationService,
		private router: Router,
		private dialog: MatDialog
	) { }

	ngOnInit() {
		//do no try to be smart
		//when any workflowable is updated, refresh the whole menu
		//that's because rules may change workflow states on any other workflowable
		//it may also add/remove forms and events
		this.crfChangeService.updatedWorkflowable$.pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(typedWorkflowable => {
			if(typedWorkflowable.entity === WorkflowableEntity.SCOPE) {
				this.scope = typedWorkflowable.workflowable as ScopeDTO;
			}
			this.refresh();
		});
	}

	ngOnChanges() {
		this.refresh();
	}

	refresh() {
		combineLatest([
			this.formService.searchOnScope(this.scope.pk),
			this.eventService.search(this.scope.pk),
			this.activatedRoute.queryParams
		]).subscribe(([forms, events, queryParams]) => {
			this.scopeForms = forms;
			this.events = events;
			this.updateEventGroups();
			//retrieve and manage expanded event pks
			const expandedEventPks: string = queryParams[SideMenuComponent.EXPANDED_EVENT_PKS_PARAMETER] ?? '';
			this.expandedEventPks = expandedEventPks.split(',').filter(p => !!p).map(p => parseInt(p));
			//manage selected event pk parameter
			/*const eventPk = parameters['eventPk'] ? parseInt(parameters['eventPk']) : undefined;
			if(eventPk && !this.expandedEventPks.includes(eventPk)) {
				this.expandedEventPks.push(eventPk);
			}*/
			this.expandedEventPks.forEach(eventPk => {
				if(!this.eventsForms[eventPk]) {
					this.formService.searchOnEvent(this.scope.pk, eventPk).subscribe(forms => {
						this.eventsForms[eventPk] = forms;
					});
				}
			});
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
		if([SideMenuComponent.OTHER_EVENT_GROUP.id, SideMenuComponent.ALL_EVENT_GROUP.id].includes(eventGroupId)) {
			return this.events.filter(e => !e.model.eventGroupId);
		}
		return this.events.filter(e => e.model.eventGroupId === eventGroupId);
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
