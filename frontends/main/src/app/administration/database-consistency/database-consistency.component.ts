import {AfterViewInit, ChangeDetectionStrategy, Component, ViewChild, signal} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MatButton} from '@angular/material/button';
import {MatCheckbox} from '@angular/material/checkbox';
import {MatDivider} from '@angular/material/divider';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatPaginator} from '@angular/material/paginator';
import {MatOption, MatSelect} from '@angular/material/select';
import {MatSort, MatSortModule} from '@angular/material/sort';
import {MatTableDataSource, MatTableModule} from '@angular/material/table';
import {MatTooltip} from '@angular/material/tooltip';
import {MatToolbar, MatToolbarRow} from '@angular/material/toolbar';
import {DatabaseService} from '@core/services/database.service';
import {getInconsistencyStatusDisplay} from './inconsistency-status-display';
import {getInconsistencyTypeDisplay} from './inconsistency-type-display';
import {MatProgressBar} from '@angular/material/progress-bar';
import {InconsistentEntity} from '@core/model/inconsistent-entity';
import {InconsistencyStatus} from '@core/model/inconsistency-status';
import {ConfigurationInconsistencyGroup} from '@core/model/configuration-inconsistency-group';
import {DenormalizationInconsistencyGroup} from '@core/model/denormalization-inconsistency-group';
import {ConfigurationInconsistencyType} from '@core/model/configuration-inconsistency-type';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	templateUrl: './database-consistency.component.html',
	styleUrl: './database-consistency.component.css',
	imports: [
		ReactiveFormsModule,
		MatButton,
		MatCheckbox,
		MatDivider,
		MatFormField,
		MatLabel,
		MatOption,
		MatPaginator,
		MatSelect,
		MatSortModule,
		MatTableModule,
		MatTooltip,
		MatToolbar,
		MatToolbarRow,
		MatProgressBar
	]
})
export class DatabaseConsistencyComponent implements AfterViewInit {
	getInconsistencyStatusDisplay = getInconsistencyStatusDisplay;
	getInconsistencyTypeDisplay = getInconsistencyTypeDisplay;

	//configuration inconsistencies
	readonly configurationLoading = signal(false);

	readonly entityOptions = Object.values(InconsistentEntity) as InconsistentEntity[];
	readonly typeOptions = Object.values(ConfigurationInconsistencyType) as ConfigurationInconsistencyType[];
	readonly statusOptions = Object.values(InconsistencyStatus) as InconsistencyStatus[];

	filterForm = new FormGroup({
		entity: new FormControl<InconsistentEntity[]>([], {nonNullable: true}),
		type: new FormControl<ConfigurationInconsistencyType[]>([], {nonNullable: true}),
		status: new FormControl<InconsistencyStatus[]>([], {nonNullable: true})
	});

	@ViewChild('configPaginator') configPaginator!: MatPaginator;
	@ViewChild('configSort') configSort!: MatSort;
	configurationInconsistencyGroups = new MatTableDataSource<ConfigurationInconsistencyGroup>([]);
	configurationInconsistencyStatus = signal('Run the configuration consistency check to detect inconsistencies');
	configurationDryRun = signal(true);

	protected readonly configDisplayedColumns = ['entity', 'modelId', 'type', 'missingEntityId', 'count', 'status'];

	//denormalization inconsistencies
	readonly denormalizationLoading = signal(false);

	@ViewChild('denormalizationPaginator') denormalizationPaginator!: MatPaginator;
	@ViewChild('denormalizationSort') denormalizationSort!: MatSort;
	denormalizationInconsistencyGroups = new MatTableDataSource<DenormalizationInconsistencyGroup>([]);
	denormalizationInconsistencyStatus = signal('Run the denormalization consistency check to detect inconsistencies');
	denormalizationDryRun = signal(true);

	protected readonly denormalizationDisplayedColumns = ['entity', 'modelId', 'count', 'status'];

	constructor(
		private databaseService: DatabaseService
	) {
		this.configurationInconsistencyGroups.filterPredicate = (data: ConfigurationInconsistencyGroup, filter: string) => {
			const {entity, type, status} = JSON.parse(filter) as {
				entity: InconsistentEntity[];
				type: ConfigurationInconsistencyType[];
				status: InconsistencyStatus[];
			};
			if(entity.length > 0 && !entity.includes(data.entity)) {
				return false;
			}
			if(type.length > 0 && !type.includes(data.type)) {
				return false;
			}
			if(status.length > 0 && !status.includes(data.status)) {
				return false;
			}
			return true;
		};
	}

	search() {
		const value = this.filterForm.value;
		const {entity, type, status} = value;
		const hasFilter = (entity?.length ?? 0) > 0 || (type?.length ?? 0) > 0 || (status?.length ?? 0) > 0;
		this.configurationInconsistencyGroups.filter = hasFilter ? JSON.stringify(value) : '';
	}

	resetFilters() {
		this.filterForm.reset();
		this.configurationInconsistencyGroups.filter = '';
	}

	ngAfterViewInit() {
		this.configurationInconsistencyGroups.paginator = this.configPaginator;
		this.configurationInconsistencyGroups.sort = this.configSort;
		this.denormalizationInconsistencyGroups.paginator = this.denormalizationPaginator;
		this.denormalizationInconsistencyGroups.sort = this.denormalizationSort;
	}

	runConfigurationConsistency() {
		this.configurationLoading.set(true);
		this.databaseService.fixConfigConsistency(this.configurationDryRun()).subscribe({
			next: result => {
				this.configurationInconsistencyGroups.data = result;
				if(result.length === 0) {
					this.configurationInconsistencyStatus.set('No configuration inconsistencies detected');
				}
				else {
					this.configurationInconsistencyStatus.set(`${result.length} configuration inconsistencies detected`);
				}
				this.configurationLoading.set(false);
			},
			error: () => this.configurationLoading.set(false)
		});
	}

	runDenormalizationConsistency() {
		this.denormalizationLoading.set(true);
		this.databaseService.fixDenormalizationConsistency(this.denormalizationDryRun()).subscribe({
			next: result => {
				this.denormalizationInconsistencyGroups.data = result;
				if(result.length === 0) {
					this.denormalizationInconsistencyStatus.set('No denormalization inconsistencies detected');
				}
				else {
					this.denormalizationInconsistencyStatus.set(`${result.length} denormalization inconsistencies detected`);
				}
				this.denormalizationLoading.set(false);
			},
			error: () => this.denormalizationLoading.set(false)
		});
	}
}
