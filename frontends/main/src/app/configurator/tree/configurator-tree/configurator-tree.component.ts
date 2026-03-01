import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {ScopeModelsTreeComponent} from '../scope-models-tree/scope-models-tree.component';
import {DatasetModelsTreeComponent} from '../dataset-models-tree/dataset-models-tree.component';
import {ProjectSettingsTreeComponent} from '../project-settings-tree/project-settings-tree.component';
import {ValidatorTreeComponent} from '../validator-tree/validator-tree.component';
import {WorkflowTreeComponent} from '../workflow-tree/workflow-tree.component';
import {ProfileTreeComponent} from '../profile-tree/profile-tree.component';
import {FeatureTreeComponent} from '../feature-tree/feature-tree.component';
import {Observable, of} from 'rxjs';
import {PrivacyPolicyTreeComponent} from '../privacy-policy-tree/privacy-policy-tree.component';
import {ResourceCategoryTreeComponent} from '../resource-category-tree/resource-category-tree.component';
import {ReportTreeComponent} from '../report-tree/report-tree.component';

@Component({
	selector: 'app-configurator-tree',
	standalone: true,
	imports: [
		CommonModule,
		MatIconModule,
		ProjectSettingsTreeComponent,
		ScopeModelsTreeComponent,
		DatasetModelsTreeComponent,
		ValidatorTreeComponent,
		WorkflowTreeComponent,
		ProfileTreeComponent,
		FeatureTreeComponent,
		PrivacyPolicyTreeComponent,
		ResourceCategoryTreeComponent,
		ReportTreeComponent
	],
	templateUrl: './configurator-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class ConfiguratorTreeComponent {
	@Input() projectId = '';
	@Input() selectedNode: string | null = null;
	@Input() scopeModels: any[] = [];
	@Input() eventModels: any[] = [];
	@Input() eventGroups: any[] = [];
	@Input() datasetModels: any[] = [];
	@Input() fieldModels: any[] = [];
	@Input() validators: any[] = [];
	@Input() workflows: any[] = [];
	@Input() workflowStates: any[] = [];
	@Input() workflowActions: any[] = [];
	@Input() profiles: any[] = [];
	@Input() features: any[] = [];
	@Input() privacyPolicies: any[] = [];
	@Input() resourceCategories: any[] = [];
	@Input() reports: any[] = [];
	@Input() selectedScopeModelId: string | null = null;
	@Input() selectedEventModelId: string | null = null;
	@Input() selectedEventGroupId: string | null = null;
	@Input() selectedDatasetModelId: string | null = null;
	@Input() selectedFieldModelId: string | null = null;
	@Input() selectedValidatorId: string | null = null;
	@Input() selectedWorkflowId: string | null = null;
	@Input() selectedWorkflowStateId: string | null = null;
	@Input() selectedWorkflowActionId: string | null = null;
	@Input() selectedProfileId: string | null = null;
	@Input() selectedFeatureId: string | null = null;
	@Input() selectedPrivacyPolicyId: string | null = null;
	@Input() selectedResourceCategoryId: string | null = null;
	@Input() selectedReportId: string | null = null;
	@Input() canNavigate?: () => Observable<boolean>;

	@Output() categoryClicked = new EventEmitter<string>();

	expandedCategory: 'scope-models' | 'dataset-models' | 'validators' | 'workflows' | 'profiles' | 'features' | 'privacy-policies' | 'resource-categories' | 'reports' | null = null;

	private runGuarded(action: () => void): void {
		const guard$ = this.canNavigate ? this.canNavigate() : of(true);
		guard$.subscribe(canNavigate => {
			if(canNavigate) {
				action();
			}
		});
	}

	onScopeModelsClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'scope-models' ? null : 'scope-models';
			this.categoryClicked.emit('scope-models');
		});
	}

	onDatasetModelsClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'dataset-models' ? null : 'dataset-models';
			this.categoryClicked.emit('dataset-models');
		});
	}

	onValidatorsClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'validators' ? null : 'validators';
			this.categoryClicked.emit('validators');
		});
	}

	onWorkflowsClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'workflows' ? null : 'workflows';
			this.categoryClicked.emit('workflows');
		});
	}

	onProfilesClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'profiles' ? null : 'profiles';
			this.categoryClicked.emit('profiles');
		});
	}

	onFeaturesClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'features' ? null : 'features';
			this.categoryClicked.emit('features');
		});
	}

	onPrivacyPolicyClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'privacy-policies' ? null : 'privacy-policies';
			this.categoryClicked.emit('privacy-policies');
		});
	}

	onResourceCategoryClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'resource-categories' ? null : 'resource-categories';
			this.categoryClicked.emit('resource-categories');
		});
	}

	onReportClicked(): void {
		this.runGuarded(() => {
			this.expandedCategory = this.expandedCategory === 'reports' ? null : 'reports';
			this.categoryClicked.emit('reports');
		});
	}

	onCategoryClick(categoryId: string): void {
		this.runGuarded(() => {
			this.expandedCategory = null;
			this.categoryClicked.emit(categoryId);
		});
	}
}
