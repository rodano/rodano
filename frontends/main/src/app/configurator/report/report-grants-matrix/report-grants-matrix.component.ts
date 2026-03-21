import {Observable} from 'rxjs';
import {MatSnackBar} from '@angular/material/snack-bar';
import {LanguageService} from '../../services/language.service';
import {ReportGrantsService} from '../../services/api/report-grants.service';
import {Component, Input} from '@angular/core';
import {BaseGrantsMatrixComponent} from '../../shared/grants-matrix/grants-matrix.component';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatButtonModule} from '@angular/material/button';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {Report} from '@core/model/report';

@Component({
	selector: 'app-report-grants-matrix',
	standalone: true,
	templateUrl: '../../shared/grants-matrix/grants-matrix.component.html',
	styleUrls: ['../../shared/matrix-shared.css', '../../shared/list-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule]
})
export class ReportGrantsMatrixComponent extends BaseGrantsMatrixComponent {
	@Input() reports: Report[] = [];

	constructor(
		languageService: LanguageService,
		private reportGrantsService: ReportGrantsService,
		snackBar: MatSnackBar
	) {
		super(languageService, snackBar);
	}

	readonly title = 'Report Rights Matrix';
	readonly subtitle = 'Define which profiles have access to each report';
	readonly themeClass = 'theme-report';
	get entities(): any[] {return this.reports;}
	getEntityId(e: Report): string {return e.reportId;}
	getEntityLabel(e: Report): string {return this.languageService.getTranslatedName(e.shortname);}
	getEntityCode(e: Report): string {return e.id;}
	getEntityCount(): number {return this.reports.length;}
	loadGrantsFromApi(): Observable<Record<string, string[]>> {return this.reportGrantsService.getReportGrants(this.projectId);}
	saveGrantsToApi(grants: Record<string, string[]>): Observable<void> {return this.reportGrantsService.saveReportGrants(this.projectId, grants);}
}
