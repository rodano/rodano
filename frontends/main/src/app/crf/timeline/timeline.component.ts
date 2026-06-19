import {Component, OnInit, input, signal} from '@angular/core';
import {ScopeService} from '@core/services/scope.service';
import {Scope} from '@core/model/scope';
import {TimelineGraphData} from '@core/model/timeline-graph-data';
import {Timeline} from '@rodano/timeline';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {MatCardModule} from '@angular/material/card';
import {SafeHtmlPipe} from '../../pipes/safe-html.pipe';
import {LoggingService} from '@core/services/logging.service';

@Component({
	selector: 'app-timeline',
	templateUrl: './timeline.component.html',
	styleUrls: ['./timeline.component.css'],
	imports: [
		MatCardModule,
		LocalizeMapPipe,
		SafeHtmlPipe
	]
})
export class TimelineComponent implements OnInit {
	readonly scope = input.required<Scope>();
	readonly graphs = signal<TimelineGraphData[]>([]);

	constructor(
		private scopeService: ScopeService,
		private loggingService: LoggingService
	) { }

	ngOnInit() {
		this.scopeService.getGraphs(this.scope().pk).subscribe(graphs => {
			this.graphs.set(graphs);

			setTimeout(() => {
				this.graphs().forEach(graph => {
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
