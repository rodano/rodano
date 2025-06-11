import {Component, Input, OnInit} from '@angular/core';
import {ScopeService} from '@core/services/scope.service';
import {ScopeDTO} from '@core/model/scope-dto';
import {TimelineGraphDataDTO} from '@core/model/timeline-graph-data-dto';
import {Timeline} from '@rodano/timeline';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {MatCardModule} from '@angular/material/card';
import {SafeHtmlPipe} from 'src/app/pipes/safe-html.pipe';
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
	@Input() scope: ScopeDTO;
	graphs: TimelineGraphDataDTO[] = [];

	constructor(
		private scopeService: ScopeService,
		private loggingService: LoggingService
	) { }

	ngOnInit() {
		this.scopeService.getGraphs(this.scope.pk).subscribe(graphs => {
			this.graphs = graphs;

			setTimeout(() => {
				this.graphs.forEach(graph => {
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
