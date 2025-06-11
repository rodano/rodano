import {Component, Input} from '@angular/core';
import {Observable} from 'rxjs';
import {MatTableModule} from '@angular/material/table';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {DownloadDirective} from '../../directives/download.component';
import {MatButton} from '@angular/material/button';
import {MatToolbar, MatToolbarRow} from '@angular/material/toolbar';
import {ReactiveFormsModule} from '@angular/forms';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {RouterLink} from '@angular/router';
import {MatOption, MatSelect} from '@angular/material/select';
import {ScopeCodeShortnamePipe} from 'src/app/pipes/scope-code-shortname.pipe';
import {MatPaginator} from '@angular/material/paginator';
import {MatProgressBar} from '@angular/material/progress-bar';
import {ScopeBreadcrumbComponent} from '../../scope/breadcrumb/scope-breadcrumb.component';
import {ExportButton} from '../summary/export-button';
import {SummaryDTO} from '@core/model/summary-dto';
import {SummaryWidgetComponent} from '../summary/summary-widget.component';
import {MatDivider} from '@angular/material/divider';

@Component({
	selector: 'app-workflow-summary-widget',
	templateUrl: '../summary/summary-widget.component.html',
	styleUrls: ['../summary/summary-widget.component.css'],
	imports: [
		MatTableModule,
		MatPaginator,
		MatFormField,
		MatLabel,
		MatToolbar,
		MatProgressBar,
		MatSelect,
		MatOption,
		ReactiveFormsModule,
		MatToolbarRow,
		MatButton,
		DownloadDirective,
		LocalizeMapPipe,
		RouterLink,
		MatDivider,
		ScopeCodeShortnamePipe,
		ScopeBreadcrumbComponent
	]
})
export class WorkflowSummaryWidgetComponent extends SummaryWidgetComponent {
	@Input() id: string;

	getData(scopePk: number): Observable<SummaryDTO> {
		return this.widgetService.getWorkflowSummary(this.id, scopePk);
	}

	getButtons(scopePk: number): ExportButton[] {
		return [
			{label: 'Export', url: this.widgetService.getWorkflowSummaryExportUrl(this.id, scopePk)},
			{label: 'Export historical detail', url: this.widgetService.getWorkflowSummaryExportHistoricalUrl(this.id, scopePk)}
		];
	}
}
