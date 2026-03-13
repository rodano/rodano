import {ChangeDetectionStrategy, Component, DestroyRef, OnInit, signal} from '@angular/core';
import {forkJoin} from 'rxjs';
import {ExtractService} from '@core/services/extract.service';
import {LocalizeMapPipe} from '../pipes/localize-map.pipe';
import {MatTabsModule} from '@angular/material/tabs';
import {ScopeMini} from '@core/model/scope-mini';
import {ConfigurationService} from '@core/services/configuration.service';
import {ScopeModel} from '@core/model/scope-model';
import {ReportService} from '@core/services/report.service';
import {MatButton} from '@angular/material/button';
import {MatFormField, MatLabel} from '@angular/material/input';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatListModule} from '@angular/material/list';
import {MatCheckbox} from '@angular/material/checkbox';
import {DownloadDirective} from '../directives/download.component';
import {MatToolbar, MatToolbarRow} from '@angular/material/toolbar';
import {nonEmptyArrayValidator} from '../validators/not-empty-array.validator';
import {DatasetModel} from '@core/model/dataset-model';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {ScopePickerComponent} from '../scope-picker/scope-picker.component';
import {MeService} from '@core/services/me.service';
import {FeatureStatic} from '@core/model/feature-static';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	templateUrl: './extract.component.html',
	styleUrls: ['./extract.component.css'],
	imports: [
		RouterLink,
		MatTabsModule,
		MatButton,
		MatLabel,
		MatListModule,
		ReactiveFormsModule,
		MatFormField,
		MatCheckbox,
		DownloadDirective,
		LocalizeMapPipe,
		MatToolbar,
		MatToolbarRow,
		LocalizeMapPipe,
		ScopePickerComponent
	]
})
export class ExtractComponent implements OnInit {
	readonly selectedScopeModel = signal<ScopeModel | undefined>(undefined);

	readonly rootScopes = signal<ScopeMini[]>([]);
	readonly scopeModels = signal<ScopeModel[]>([]);
	readonly datasetModels = signal<DatasetModel[]>([]);

	extractForm = new FormGroup({
		rootScopePk: new FormControl(0, {nonNullable: true, validators: [Validators.required]}),
		withModificationDates: new FormControl(false, {nonNullable: true}),
		datasetModelIds: new FormControl([] as string[], {
			nonNullable: true,
			validators: [Validators.required, nonEmptyArrayValidator]
		})
	});

	constructor(
		private configurationService: ConfigurationService,
		private meService: MeService,
		private extractService: ExtractService,
		private reportService: ReportService,
		private activatedRoute: ActivatedRoute,
		private destroyRef: DestroyRef
	) {}

	ngOnInit() {
		this.activatedRoute.params.pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(params => {
			if(this.scopeModels().length > 0) {
				this.selectScopeModel(params['scopeModelId']);
			}
		});

		forkJoin({
			rootScopes: this.meService.getScopes(FeatureStatic.EXPORT, true, false),
			scopeModels: this.configurationService.getScopeModelsSorted()
		}).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(({rootScopes, scopeModels}) => {
			scopeModels.reverse();
			this.rootScopes.set(rootScopes);
			this.scopeModels.set(scopeModels);
			this.extractForm.get('rootScopePk')?.setValue(this.rootScopes()[0].pk);
			this.selectScopeModel(this.activatedRoute.snapshot.params['scopeModelId']);
		});
	}

	selectScopeModel(scopeModelId: string) {
		this.selectedScopeModel.set(this.scopeModels().find(s => s.id === scopeModelId) ?? this.scopeModels()[0]);
		this.extractService.getDatasetModels(this.selectedScopeModel()!.id).subscribe(d => this.datasetModels.set(d));
	}

	getExportUrl(): string {
		const datasetModelIds = this.extractForm.get('datasetModelIds')?.value as string[];
		const rootScopePk = this.extractForm.get('rootScopePk')?.value;
		const withModificationDates = this.extractForm.get('withModificationDates')?.value;
		return this.extractService.getExportUrl(datasetModelIds, rootScopePk, withModificationDates);
	}

	getSpecificationsUrl(): string {
		const datasetModelIds = this.extractForm.get('datasetModelIds')?.value as string[];
		const withModificationDates = this.extractForm.get('withModificationDates')?.value;
		return this.extractService.getSpecificationsUrl(datasetModelIds, withModificationDates);
	}

	getScopeTransfersReportUrl(): string {
		const rootScopePk = this.extractForm.get('rootScopePk')?.value;
		return this.reportService.getScopeTransfersUrl(this.selectedScopeModel()!.id, rootScopePk);
	}

	getEventsReportUrl(): string {
		const rootScopePk = this.extractForm.get('rootScopePk')?.value;
		return this.reportService.getEventsUrl(this.selectedScopeModel()!.id, rootScopePk);
	}
}
