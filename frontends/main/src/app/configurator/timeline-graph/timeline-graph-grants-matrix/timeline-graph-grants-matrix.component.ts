import {Observable} from 'rxjs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {LanguageService} from '../../services/language.service';
import {Component, Input} from '@angular/core';
import {BaseGrantsMatrixComponent} from '../../shared/grants-matrix/grants-matrix.component';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatButtonModule} from '@angular/material/button';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {TimelineGraph} from '@core/model/timeline-graph';
import {TimelineGraphGrantsService} from '../../services/api/timeline-graph-grants.service';

@Component({
	selector: 'app-timeline-graph-grants-matrix',
	standalone: true,
	templateUrl: '../../shared/grants-matrix/grants-matrix.component.html',
	styleUrls: ['../../shared/matrix-shared.css', '../../shared/list-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule]
})
export class TimelineGraphGrantsMatrixComponent extends BaseGrantsMatrixComponent {
	@Input() timelineGraphs: TimelineGraph[] = [];

	constructor(
		languageService: LanguageService,
		private timelineGraphGrantsService: TimelineGraphGrantsService,
		snackBar: MatSnackBar
	) {
		super(languageService, snackBar);
	}

	readonly title = 'Timeline Graph Rights Matrix';
	readonly subtitle = 'Define which profiles have access to each timeline graph';
	readonly themeClass = 'theme-timeline-graph';
	get entities(): any[] {return this.timelineGraphs;}
	getEntityId(e: TimelineGraph): string {return e.timelineGraphId;}
	getEntityLabel(e: TimelineGraph): string {return this.languageService.getTranslatedName(e.shortname);}
	getEntityCode(e: TimelineGraph): string {return e.id;}
	getEntityCount(): number {return this.timelineGraphs.length;}
	loadGrantsFromApi(): Observable<Record<string, string[]>> {return this.timelineGraphGrantsService.getTimelineGraphGrants(this.projectId);}
	saveGrantsToApi(grants: Record<string, string[]>): Observable<void> {return this.timelineGraphGrantsService.saveTimelineGraphGrants(this.projectId, grants);}
}
