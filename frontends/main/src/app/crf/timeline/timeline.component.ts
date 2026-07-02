import {Component, OnInit, input, signal} from '@angular/core';
import {Router} from '@angular/router';
import {ScopeService} from '@core/services/scope.service';
import {Scope} from '@core/model/scope';
import {TimelineGraphData} from '@core/model/timeline-graph-data';
import {Timeline} from '@rodano/timeline';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {SafeHtmlPipe} from '../../pipes/safe-html.pipe';
import {LoggingService} from '@core/services/logging.service';
import {CRF_PATH} from '../crf-routes';

@Component({
	selector: 'app-timeline',
	templateUrl: './timeline.component.html',
	styleUrls: ['./timeline.component.css'],
	imports: [
		LocalizeMapPipe,
		SafeHtmlPipe
	]
})
export class TimelineComponent implements OnInit {
	readonly scope = input.required<Scope>();
	readonly timelines = signal<TimelineGraphData[]>([]);

	constructor(
		private router: Router,
		private scopeService: ScopeService,
		private loggingService: LoggingService
	) { }

	ngOnInit() {
		this.scopeService.getTimelines(this.scope().pk).subscribe(timelines => {
			this.timelines.set(timelines);

			//add links to values
			this.timelines().forEach(graph => {
				(graph.sections ?? []).forEach(section => {
					(section.values ?? []).forEach(value => {
						const commands: unknown[] = [];
						const queryParams: Record<string, unknown> = {};
						if(value.metadata) {
							if(value.metadata['scopePk']) {
								commands.push(`/${CRF_PATH}`, value.metadata['scopePk']);
							}
							if(value.metadata['eventPk']) {
								commands.push('events', value.metadata['eventPk']);
								queryParams['expandedEventPks'] = value.metadata['eventPk'];
							}
							if(value.metadata['formPk']) {
								commands.push('forms', value.metadata['formPk']);
							}
						}
						if(commands.length > 0 && !value.link) {
							value.link = this.router.serializeUrl(this.router.createUrlTree(commands, {queryParams}));
						}
					});
				});
			});

			setTimeout(() => {
				this.timelines().forEach(graph => {
					const container = document.getElementById(graph.id) as HTMLDivElement;
					try {
						new Timeline(container, graph, 'en-US').draw();
					}
					catch (e) {
						this.loggingService.error(`Error drawing timeline ${graph.id}`, e);
					}
				});
			}, 0);
		});
	}
}
