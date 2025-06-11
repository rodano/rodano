import {Component, ViewChild, Input, OnInit, DestroyRef} from '@angular/core';
import {WidgetService} from '@core/services/widget.service';
import {forkJoin, Observable, of, Subject} from 'rxjs';
import {MatTable, MatTableDataSource, MatTableModule} from '@angular/material/table';
import {switchMap, startWith} from 'rxjs/operators';
import {ScopeDTO} from '@core/model/scope-dto';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {DownloadDirective} from '../../directives/download.component';
import {MatButton} from '@angular/material/button';
import {MatToolbar, MatToolbarRow} from '@angular/material/toolbar';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {RouterLink} from '@angular/router';
import {ConfigurationService} from '@core/services/configuration.service';
import {ScopeModelDTO} from '@core/model/scope-model-dto';
import {MatOption, MatSelect} from '@angular/material/select';
import {ScopeCodeShortnamePipe} from 'src/app/pipes/scope-code-shortname.pipe';
import {ScopeRelationsService} from '@core/services/scope-relations.service';
import {MatPaginator} from '@angular/material/paginator';
import {MatProgressBar} from '@angular/material/progress-bar';
import {ScopeBreadcrumbComponent} from '../../scope/breadcrumb/scope-breadcrumb.component';
import {ScopeTinyDTO} from '@core/model/scope-tiny-dto';
import {SummaryRowDTO} from '@core/model/summary-row-dto';
import {SummaryColumnDTO} from '@core/model/summary-column-dto';
import {ExportButton} from './export-button';
import {SummaryDTO} from '@core/model/summary-dto';
import {MatDivider} from '@angular/material/divider';
import {MatTooltip} from '@angular/material/tooltip';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';

@Component({
	templateUrl: './summary-widget.component.html',
	styleUrls: ['./summary-widget.component.css'],
	imports: [
		MatTableModule,
		MatPaginator,
		MatFormField,
		MatLabel,
		MatToolbar,
		MatTooltip,
		MatProgressBar,
		MatSelect,
		MatOption,
		ReactiveFormsModule,
		MatToolbarRow,
		MatDivider,
		MatButton,
		DownloadDirective,
		LocalizeMapPipe,
		RouterLink,
		ScopeCodeShortnamePipe,
		ScopeBreadcrumbComponent
	]
})
export abstract class SummaryWidgetComponent implements OnInit {
	@Input() scopes?: ScopeDTO[];

	loading = true;

	//scope selector
	control: FormControl;

	rootScopeModel: ScopeModelDTO;
	leafScopeModel: ScopeModelDTO;

	//table
	ancestors: (ScopeTinyDTO | ScopeDTO)[];
	columns: SummaryColumnDTO[];
	@ViewChild(MatTable, {static: true}) table: MatTable<SummaryRowDTO>;
	@ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
	dataSource = new MatTableDataSource<SummaryRowDTO>([]);
	columnsToDisplay: string[] = [];

	buttons: ExportButton[] = [];

	rootScopePkChanged: Subject<number> = new Subject<number>();

	constructor(
		private configurationService: ConfigurationService,
		private scopeRelationService: ScopeRelationsService,
		protected widgetService: WidgetService,
		private destroyRef: DestroyRef
	) {}

	ngOnInit() {
		this.control = new FormControl(this.getInitialScopePk());
		this.control.valueChanges.subscribe(v => this.rootScopePkChanged.next(v));

		//leaf and root scope models are required for this component to work
		forkJoin({
			leafScopeModel: this.configurationService.getLeafScopeModel(),
			rootScopeModel: this.configurationService.getRootScopeModel()
		}).subscribe(({leafScopeModel, rootScopeModel}) => {
			this.leafScopeModel = leafScopeModel;
			this.rootScopeModel = rootScopeModel;

			//technically, it is possible to start fetching data before leaf and root scope models are available
			//but then the readability of the RxJS code is not good
			this.rootScopePkChanged.pipe(
				takeUntilDestroyed(this.destroyRef),
				startWith(this.getInitialScopePk()),
				switchMap(scopePk => {
					this.loading = true;
					return this.getData(scopePk);
				}),
				switchMap(data => {
					const rootScopePk = data.rows[0].scope.pk;
					this.buttons = this.getButtons(rootScopePk);
					return forkJoin({
						data: of(data),
						ancestors: this.scopeRelationService.getAncestors(rootScopePk)
					});
				})
			).subscribe(({data, ancestors}) => {
				this.columns = data.columns;
				this.columnsToDisplay = ['scope', ...data.columns.map(c => c.id)];
				this.dataSource = new MatTableDataSource<SummaryRowDTO>(data.rows);
				this.dataSource.paginator = this.paginator;
				this.ancestors = [...ancestors, this.getRootScope()];
				this.loading = false;
			});
		});
	}

	abstract getData(scopePk: number | undefined): Observable<SummaryDTO>;

	abstract getButtons(scopePk: number | undefined): ExportButton[];

	getInitialScopePk(): number | undefined {
		return this.scopes?.[0].pk;
	}

	getRootScope(): ScopeTinyDTO {
		return this.dataSource.data[0].scope;
	}

	getParent(): ScopeTinyDTO | ScopeDTO {
		const depth = this.ancestors.length;
		return this.ancestors[depth - 2];
	}

	selectScope(scope: ScopeTinyDTO | ScopeDTO) {
		this.rootScopePkChanged.next(scope.pk);
	}

	getRawValue(row: SummaryRowDTO, column: SummaryColumnDTO): number {
		if(column.total) {
			return row.total;
		}
		return row.values[column.id];
	}

	getValue(row: SummaryRowDTO, column: SummaryColumnDTO): string {
		const value = this.getRawValue(row, column);
		return value !== undefined ? value.toString() : 'Not implemented';
	}

	getValuePercent(row: SummaryRowDTO, column: SummaryColumnDTO): string {
		const value = this.getRawValue(row, column);
		const percent = row.total === 0 ? 0 : Math.round(100 * value / row.total);
		return percent.toString();
	}
}
