import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {SectionConfigDialogComponent} from '../../dialogs/widget/section-config-dialog/section-config-dialog.component';
import {WidgetConfigDialogComponent, WidgetConfigDialogData} from '../../dialogs/widget/widget-config-dialog/widget-config-dialog.component';
import {Section} from '@core/model/section';
import {Widget} from '@core/model/widget';
import {WidgetTypeDef} from '../../shared/layout-widget-types';
import {LanguageService} from '../language.service';
import {FeatureManagerService} from '../manager/feature-manager.service';
import {ScopeModelManagerService} from '../manager/scope-model-manager.service';
import {EventModelManagerService} from '../manager/event-model-manager.service';
import {DatasetModelManagerService} from '../manager/dataset-model-manager.service';
import {FormModelManagerService} from '../manager/form-model-manager.service';
import {ProfileManagerService} from '../manager/profile-manager.service';
import {ChartManagerService} from '../manager/chart-manager.service';
import {WorkflowWidgetManagerService} from '../manager/workflow-widget-manager.service';
import {WorkflowSummaryManagerService} from '../manager/workflow-summary-manager.service';
import {ResourceCategoryManagerService} from '../manager/resource-category-manager.service';

@Injectable({
	providedIn: 'root'
})
export class LayoutDialogService {
	constructor(
		private dialog: MatDialog,
		private languageService: LanguageService,
		private featureManager: FeatureManagerService,
		private scopeModelManager: ScopeModelManagerService,
		private eventModelManager: EventModelManagerService,
		private datasetModelManager: DatasetModelManagerService,
		private formModelManager: FormModelManagerService,
		private profileManager: ProfileManagerService,
		private chartManager: ChartManagerService,
		private workflowWidgetManager: WorkflowWidgetManagerService,
		private workflowSummaryManager: WorkflowSummaryManagerService,
		private resourceCategoryManager: ResourceCategoryManagerService
	) {}

	openSectionConfig(section: Section): Observable<any> {
		return this.dialog.open(SectionConfigDialogComponent, {
			width: '500px',
			data: {
				section,
				languages: this.languageService.projectLanguages,
				features: this.featureManager.getAll(),
				scopeModels: this.scopeModelManager.getAll(),
				eventModels: this.eventModelManager.getAll(),
				datasetModels: this.datasetModelManager.getAll(),
				formModels: this.formModelManager.getAll(),
				profiles: this.profileManager.getAll()
			}
		}).afterClosed();
	}

	openWidgetConfig(widget: Widget, typeDef: WidgetTypeDef): Observable<any> {
		return this.dialog.open(WidgetConfigDialogComponent, {
			width: '500px',
			data: {
				widget,
				typeDef,
				charts: this.chartManager.getAll(),
				workflowWidgets: this.workflowWidgetManager.getAll(),
				workflowSummaries: this.workflowSummaryManager.getAll(),
				resourceCategories: this.resourceCategoryManager.getAll(),
				features: this.featureManager.getAll(),
				scopeModels: this.scopeModelManager.getAll(),
				eventModels: this.eventModelManager.getAll(),
				datasetModels: this.datasetModelManager.getAll(),
				formModels: this.formModelManager.getAll(),
				profiles: this.profileManager.getAll()
			} as WidgetConfigDialogData
		}).afterClosed();
	}
}
