import {Component, HostListener, OnInit, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {ConfiguratorService} from '../services/api/configurator.service';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {ProjectConfigVersion} from '@core/model/project-config-version';
import {ConfiguratorDetailComponent} from '../configurator-detail/configurator-detail.component';
import {ConfiguratorTreeComponent} from '../tree/configurator-tree/configurator-tree.component';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmationDialogComponent} from '../../confirmation-dialog/confirmation-dialog.component';
import {ComponentCanDeactivate} from '../../guards/unsaved-changes.guard';
import {LanguageService} from '../services/language.service';
import {SnapshotManagerService} from '../services/manager/snapshot-manager.service';
import {forkJoin, Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {EntitySaveOrchestratorService} from '../services/entity-save-orchestrator.service';
import {ScopeModelManagerService} from '../services/manager/scope-model-manager.service';
import {DatasetModelManagerService} from '../services/manager/dataset-model-manager.service';
import {ValidatorManagerService} from '../services/manager/validator-manager.service';
import {WorkflowManagerService} from '../services/manager/workflow-manager.service';
import {EventModelManagerService} from '../services/manager/event-model-manager.service';
import {EventGroupManagerService} from '../services/manager/event-group-manager.service';
import {FieldModelManagerService} from '../services/manager/field-model-manager.service';
import {WorkflowStateManagerService} from '../services/manager/workflow-state-manager.service';
import {WorkflowActionManagerService} from '../services/manager/workflow-action-manager.service';
import {ProfileManagerService} from '../services/manager/profile-manager.service';
import {FeatureManagerService} from '../services/manager/feature-manager.service';
import {PrivacyPolicyManagerService} from '../services/manager/privacy-policy-manager.service';
import {ResourceCategoryManagerService} from '../services/manager/resource-category-manager.service';
import {ReportManagerService} from '../services/manager/report-manager.service';
import {ChartManagerService} from '../services/manager/chart-manager.service';

@Component({
	selector: 'app-configurator-editor',
	standalone: true,
	templateUrl: './configurator-editor.component.html',
	styleUrls: ['./configurator-editor.component.css'],
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatProgressSpinnerModule,
		MatSnackBarModule,
		ConfiguratorTreeComponent,
		ConfiguratorDetailComponent,
		MatTooltipModule
	]
})
export class ConfiguratorEditorComponent implements OnInit, ComponentCanDeactivate {
	@ViewChild(ConfiguratorTreeComponent) treeComponent!: ConfiguratorTreeComponent;
	@ViewChild(ConfiguratorDetailComponent) detailComponent!: ConfiguratorDetailComponent;

	projectId = '';
	project: ConfiguratorProject | null = null;
	workingProject: ConfiguratorProject | null = null;
	draftVersion: ProjectConfigVersion | null = null;
	loading = true;
	saving = false;
	selectedNode: string | null = null;
	modifiedFields = new Set<string>();
	scopeModelModified = false;
	datasetModelModified = false;
	validatorModified = false;
	workflowModified = false;
	profileModified = false;
	featureModified = false;
	privacyPolicyModified = false;
	resourceCategoryModified = false;
	reportModified = false;
	chartModified = false;

	scopeModels: any[] = [];
	datasetModels: any[] = [];
	validators: any[] = [];
	eventModels: any[] = [];
	eventGroups: any[] = [];
	fieldModels: any[] = [];
	workflows: any[] = [];
	workflowStates: any[] = [];
	workflowActions: any[] = [];
	profiles: any[] = [];
	features: any[] = [];
	privacyPolicies: any[] = [];
	resourceCategories: any[] = [];
	reports: any[] = [];
	charts: any[] = [];

	selectedScopeModelId: string | null = null;
	selectedEventModelId: string | null = null;
	selectedEventGroupId: string | null = null;
	selectedDatasetModelId: string | null = null;
	selectedFieldModelId: string | null = null;
	selectedValidatorId: string | null = null;
	selectedWorkflowId: string | null = null;
	selectedWorkflowStateId: string | null = null;
	selectedWorkflowActionId: string | null = null;
	selectedProfileId: string | null = null;
	selectedFeatureId: string | null = null;
	selectedPrivacyPolicyId: string | null = null;
	selectedResourceCategoryId: string | null = null;
	selectedReportId: string | null = null;
	selectedChartId: string | null = null;

	canRollback = false;
	canRollForward = false;

	canNavigate = () => this.confirmDiscardIfChanged();

	constructor(
		public languageService: LanguageService,
		private route: ActivatedRoute,
		private router: Router,
		private configuratorService: ConfiguratorService,
		private snapshotManager: SnapshotManagerService,
		private entitySaveOrchestratorService: EntitySaveOrchestratorService,
		private scopeModelManager: ScopeModelManagerService,
		private eventModelManager: EventModelManagerService,
		private eventGroupManager: EventGroupManagerService,
		private datasetModelManager: DatasetModelManagerService,
		private fieldModelManager: FieldModelManagerService,
		private validatorManager: ValidatorManagerService,
		private workflowManager: WorkflowManagerService,
		private workflowStateManager: WorkflowStateManagerService,
		private workflowActionManager: WorkflowActionManagerService,
		private profileManager: ProfileManagerService,
		private featureManager: FeatureManagerService,
		private privacyPolicyManager: PrivacyPolicyManagerService,
		private resourceCategoryManager: ResourceCategoryManagerService,
		private reportManager: ReportManagerService,
		private chartManager: ChartManagerService,
		private snackBar: MatSnackBar,
		private dialog: MatDialog
	) {}

	ngOnInit(): void {
		this.route.paramMap.subscribe(params => {
			this.projectId = params.get('projectId') || '';
			if(this.projectId) {
				this.initializeProject();
			}
		});
	}

	canDeactivate(): boolean {
		if(this.hasModifications) {
			return confirm('You have unsaved changes. Are you sure you want to leave?');
		}
		return true;
	}

	@HostListener('window:beforeunload', ['$event'])
	unloadNotification($event: any): void {
		if(this.hasModifications) {
			$event.returnValue = true;
		}
	}

	initializeProject(): void {
		this.loading = true;
		localStorage.setItem('configProjectId', this.projectId);

		this.scopeModelManager.invalidate();
		this.eventModelManager.invalidate();
		this.eventGroupManager.invalidate();
		this.datasetModelManager.invalidate();
		this.fieldModelManager.invalidate();
		this.validatorManager.invalidate();
		this.workflowManager.invalidate();
		this.workflowStateManager.invalidate();
		this.workflowActionManager.invalidate();
		this.profileManager.invalidate();
		this.featureManager.invalidate();
		this.privacyPolicyManager.invalidate();
		this.resourceCategoryManager.invalidate();
		this.reportManager.invalidate();
		this.chartManager.invalidate();

		this.configuratorService.getProject(this.projectId).subscribe({
			next: project => {
				this.project = project;
				this.workingProject = {...project};

				if(project?.languages?.length) {
					this.languageService.setProjectLanguages(project.languages);
					const defaultLanguage = project.languages.find(l => l.isDefault)?.languageCode || project.languages[0].languageCode || 'en';
					this.languageService.setLanguage(defaultLanguage);
				}

				this.loadDraftVersion();
				this.loadTreeData();
			},
			error: error => {
				console.error('Error selecting project:', error);
				this.snackBar.open('Failed to load project', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	private loadTreeData(): void {
		this.scopeModelManager.load(this.projectId).subscribe({
			next: models => this.scopeModels = models,
			error: error => console.error('Error loading scope models:', error)
		});

		this.datasetModelManager.load(this.projectId).subscribe({
			next: models => this.datasetModels = models,
			error: error => console.error('Error loading dataset models:', error)
		});

		this.validatorManager.load(this.projectId).subscribe({
			next: validators => this.validators = validators,
			error: error => console.error('Error loading validators:', error)
		});

		this.workflowManager.load(this.projectId).subscribe({
			next: workflows => this.workflows = workflows,
			error: error => console.error('Error loading workflows:', error)
		});

		this.profileManager.load(this.projectId).subscribe({
			next: profiles => this.profiles = profiles,
			error: error => console.error('Error loading profiles:', error)
		});

		this.featureManager.load(this.projectId).subscribe({
			next: features => this.features = features,
			error: error => console.error('Error loading features:', error)
		});

		this.privacyPolicyManager.load(this.projectId).subscribe({
			next: privacyPolicies => this.privacyPolicies = privacyPolicies,
			error: error => console.error('Error loading privacy policies:', error)
		});

		this.resourceCategoryManager.load(this.projectId).subscribe({
			next: resourceCategories => this.resourceCategories = resourceCategories,
			error: error => console.error('Error loading resource categories:', error)
		});

		this.reportManager.load(this.projectId).subscribe({
			next: reports => this.reports = reports,
			error: error => console.error('Error loading reports:', error)
		});

		this.chartManager.load(this.projectId).subscribe({
			next: charts => this.charts = charts,
			error: error => console.error('Error loading charts:', error)
		});
	}

	private refreshTreeData(): void {
		this.scopeModels = this.scopeModelManager.getAll();
		this.datasetModels = this.datasetModelManager.getAll();
		this.validators = this.validatorManager.getAll();
		this.workflows = this.workflowManager.getAll();
		this.profiles = this.profileManager.getAll();
		this.features = this.featureManager.getAll();
		this.privacyPolicies = this.privacyPolicyManager.getAll();
		this.resourceCategories = this.resourceCategoryManager.getAll();
		this.reports = this.reportManager.getAll();
		this.charts = this.chartManager.getAll();
	}

	loadDraftVersion(): void {
		this.configuratorService.getOrCreateDraft(this.projectId).subscribe({
			next: draft => {
				this.draftVersion = draft;
				this.loading = false;
				this.loadSnapshotState();
			},
			error: error => {
				console.error('Error loading draft version:', error);
				this.snackBar.open('Failed to load draft', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	loadSnapshotState(): void {
		if(!this.draftVersion) {
			return;
		}

		this.snapshotManager.getSnapshotState(this.projectId, this.draftVersion.pk!).subscribe({
			next: state => {
				this.canRollback = state.canRollback;
				this.canRollForward = state.canRollForward;
			}
		});
	}

	onLanguageChange(event: Event): void {
		const select = event.target as HTMLSelectElement;
		this.languageService.setLanguage(select.value);
	}

	onNodeSelected(nodeId: string | null): void {
		this.selectedNode = nodeId;

		if(nodeId === 'scope-models' || nodeId === 'dataset-models' || nodeId === 'validators' || nodeId === 'workflows' || nodeId === 'profiles') {
			this.selectedScopeModelId = null;
			this.selectedEventModelId = null;
			this.selectedEventGroupId = null;
			this.selectedDatasetModelId = null;
			this.selectedFieldModelId = null;
			this.selectedValidatorId = null;
			this.selectedWorkflowId = null;
			this.selectedWorkflowStateId = null;
			this.selectedWorkflowActionId = null;
			this.selectedProfileId = null;
			this.selectedFeatureId = null;
			this.selectedPrivacyPolicyId = null;
			this.eventModels = [];
			this.eventGroups = [];
			this.fieldModels = [];
			this.workflows = [];
			this.workflowStates = [];
			this.workflowActions = [];
			this.profiles = [];
			this.features = [];
			this.privacyPolicies = [];
			this.resourceCategories = [];
			this.reports = [];
			this.charts = [];

			this.detailComponent?.scopeModelsListComponent?.clearSelection();
			this.detailComponent?.datasetModelsListComponent?.clearSelection();
			this.detailComponent?.validatorsListComponent?.clearSelection();
			this.detailComponent?.workflowListComponent?.clearSelection();
			this.detailComponent?.profileListComponent?.clearSelection();
			this.detailComponent?.featureListComponent?.clearSelection();
			this.detailComponent?.privacyPolicyListComponent?.clearSelection();
			this.detailComponent?.resourceCategoryListComponent?.clearSelection();
			this.detailComponent?.reportListComponent?.clearSelection();
			this.detailComponent?.chartListComponent?.clearSelection();
		}
	}

	onScopeModelsChanged(value: boolean): void {this.scopeModelModified = value;}
	onDatasetModelsChanged(value: boolean): void {this.datasetModelModified = value;}
	onValidatorsChanged(value: boolean): void {this.validatorModified = value;}
	onWorkflowsChanged(value: boolean): void {this.workflowModified = value;}
	onProfilesChanged(value: boolean): void {this.profileModified = value;}
	onFeaturesChanged(value: boolean): void {this.featureModified = value;}
	onPrivacyPoliciesChanged(value: boolean): void {this.privacyPolicyModified = value;}
	onResourceCategoriesChanged(value: boolean): void {this.resourceCategoryModified = value;}
	onReportsChanged(value: boolean): void {this.reportModified = value;}
	onChartsChanged(value: boolean): void {this.chartModified = value;}

	onFieldsUpdated(updates: Partial<ConfiguratorProject>): void {
		this.workingProject = {...this.workingProject!, ...updates};
	}

	get projectModificationCount(): number {
		if(!this.project || !this.workingProject) {
			return 0;
		}
		return Object.keys(this.workingProject).filter(key => {
			const original = this.project![key as keyof ConfiguratorProject];
			const current = this.workingProject![key as keyof ConfiguratorProject];
			if(typeof original === 'object' && original !== null) {
				return JSON.stringify(original) !== JSON.stringify(current);
			}
			return original !== current;
		}).length;
	}

	onSaveDraft(): void {
		if(!this.workingProject) {
			return;
		}

		this.saving = true;
		this.configuratorService.updateProject(this.projectId, this.workingProject).subscribe({
			next: updatedProject => {
				this.project = updatedProject;
				this.workingProject = {...updatedProject};
				this.modifiedFields.clear();

				const saveObservables: any[] = [];

				if(this.scopeModelModified) {
					saveObservables.push(this.saveScopeModelsAndEvents());
				}

				if(this.datasetModelModified) {
					saveObservables.push(this.saveDatasetModelsAndFields());
				}

				if(this.validatorModified) {
					saveObservables.push(this.saveValidators());
				}

				if(this.workflowModified) {
					saveObservables.push(this.saveWorkflows());
				}

				if(this.profileModified) {
					saveObservables.push(this.saveProfiles());
				}

				if(this.featureModified) {
					saveObservables.push(this.saveFeatures());
				}

				if(this.privacyPolicyModified) {
					saveObservables.push(this.savePrivacyPolicies());
				}

				if(this.resourceCategoryModified) {
					saveObservables.push(this.saveResourceCategories());
				}

				if(this.reportModified) {
					saveObservables.push(this.saveReports());
				}

				if(this.chartModified) {
					saveObservables.push(this.saveCharts());
				}

				if(saveObservables.length > 0) {
					forkJoin(saveObservables).subscribe({
						next: () => {
							this.resetAllModifications();
							this.saving = false;
							this.snackBar.open('Draft saved', 'Close', {duration: 2000});
						},
						error: error => {
							console.error('Error saving:', error);
							this.snackBar.open('Failed to save changes', 'Close', {duration: 3000});
							this.saving = false;
						}
					});
				}
				else {
					this.saving = false;
					this.snackBar.open('Draft saved', 'Close', {duration: 2000});
				}
			},
			error: error => {
				console.error('Error saving draft:', error);
				this.snackBar.open('Failed to save draft', 'Close', {duration: 3000});
				this.saving = false;
			}
		});
	}

	private saveScopeModelsAndEvents(): Promise<void> {
		const component = this.detailComponent?.scopeModelsListComponent;
		if(!component) {
			return Promise.resolve();
		}

		return this.entitySaveOrchestratorService.saveScopeModels(this.projectId, {
			scopeModelManager: component.scopeModelManager,
			eventModelManager: component.eventModelManager,
			eventGroupManager: component.eventGroupManager,
			scopeModels: component.scopeModels,
			eventModels: component.eventModels,
			eventGroups: component.eventGroups,
			originalScopeModels: component.originalScopeModels,
			modifiedScopeModelIds: component.modifiedScopeModelIds,
			modifiedEventModels: component.modifiedEventModels,
			modifiedEventGroups: component.modifiedEventGroups
		}).toPromise().then(() => {
			component.loadScopeModels();
			this.scopeModels = this.scopeModelManager.getAll();
		});
	}

	private saveDatasetModelsAndFields(): Promise<void> {
		const component = this.detailComponent?.datasetModelsListComponent;
		if(!component) {
			return Promise.resolve();
		}

		return this.entitySaveOrchestratorService.saveDatasetModels(this.projectId, {
			datasetModelManager: component.datasetModelManager,
			fieldModelManager: component.fieldModelManager,
			datasetModels: component.datasetModels,
			fieldModels: component.fieldModels,
			originalDatasetModels: component.originalDatasetModels,
			modifiedDatasetModelIds: component.modifiedDatasetModelIds,
			modifiedFieldModels: component.modifiedFieldModels
		}).toPromise().then(() => {
			component.loadDatasetModels();
			this.datasetModels = this.datasetModelManager.getAll();
		});
	}

	private saveValidators(): Promise<void> {
		const component = this.detailComponent?.validatorsListComponent;
		if(!component) {
			return Promise.resolve();
		}

		return this.entitySaveOrchestratorService.saveValidators(this.projectId, {
			validatorManager: component.validatorManager,
			validators: component.validators,
			originalValidators: component.originalValidators,
			modifiedValidatorIds: component.modifiedValidatorIds
		}).toPromise().then(() => {
			component.loadValidators();
			this.validators = this.validatorManager.getAll();
		});
	}

	private saveWorkflows(): Promise<void> {
		const component = this.detailComponent?.workflowListComponent;
		if(!component) {
			return Promise.resolve();
		}

		return this.entitySaveOrchestratorService.saveWorkflows(this.projectId, {
			workflowManager: component.workflowManager,
			workflowStateManager: component.workflowStateManager,
			workflowActionManager: component.workflowActionManager,
			workflows: component.workflows,
			workflowStates: component.workflowStates,
			workflowActions: component.workflowActions,
			originalWorkflows: component.originalWorkflows,
			modifiedWorkflowIds: component.modifiedWorkflowIds,
			modifiedWorkflowStateIds: component.modifiedWorkflowStates,
			modifiedWorkflowActionIds: component.modifiedWorkflowActions
		}).toPromise().then(() => {
			component.loadWorkflows();
			this.workflows = this.workflowManager.getAll();
		});
	}

	private saveProfiles(): Promise<void> {
		const component = this.detailComponent?.profileListComponent;
		if(!component) {
			return Promise.resolve();
		}

		return this.entitySaveOrchestratorService.saveProfiles(this.projectId, {
			profileManager: component.profileManager,
			profiles: component.profiles,
			originalProfiles: component.originalProfiles,
			modifiedProfileIds: component.modifiedProfileIds
		}).toPromise().then(() => {
			component.loadProfiles();
			this.profiles = this.profileManager.getAll();
		});
	}

	private saveFeatures(): Promise<void> {
		const component = this.detailComponent?.featureListComponent;
		if(!component) {
			return Promise.resolve();
		}

		return this.entitySaveOrchestratorService.saveFeatures(this.projectId, {
			featureManager: component.featureManager,
			features: component.features,
			originalFeatures: component.originalFeatures,
			modifiedFeatureIds: component.modifiedFeatureIds
		}).toPromise().then(() => {
			component.loadFeatures();
			this.features = this.featureManager.getAll();
		});
	}

	private savePrivacyPolicies(): Promise<void> {
		const component = this.detailComponent?.privacyPolicyListComponent;
		if(!component) {
			return Promise.resolve();
		}

		return this.entitySaveOrchestratorService.savePrivacyPolicies(this.projectId, {
			privacyPolicyManager: component.privacyPolicyManager,
			privacyPolicies: component.privacyPolicies,
			originalPrivacyPolicies: component.originalPrivacyPolicies,
			modifiedPrivacyPolicyIds: component.modifiedPrivacyPolicyIds
		}).toPromise().then(() => {
			component.loadPrivacyPolicies();
			this.privacyPolicies = this.privacyPolicyManager.getAll();
		});
	}

	private saveResourceCategories(): Promise<void> {
		const component = this.detailComponent?.resourceCategoryListComponent;
		if(!component) {
			return Promise.resolve();
		}

		return this.entitySaveOrchestratorService.saveResourceCategories(this.projectId, {
			resourceCategoryManager: component.resourceCategoryManager,
			resourceCategories: component.resourceCategories,
			originalResourceCategories: component.originalResourceCategories,
			modifiedResourceCategoryIds: component.modifiedResourceCategoryIds
		}).toPromise().then(() => {
			component.loadResourceCategories();
			this.resourceCategories = this.resourceCategoryManager.getAll();
		});
	}

	private saveReports(): Promise<void> {
		const component = this.detailComponent?.reportListComponent;
		if(!component) {
			return Promise.resolve();
		}

		return this.entitySaveOrchestratorService.saveReports(this.projectId, {
			reportManager: component.reportManager,
			reports: component.reports,
			originalReports: component.originalReports,
			modifiedReportIds: component.modifiedReportIds
		}).toPromise().then(() => {
			component.loadReports();
			this.reports = this.reportManager.getAll();
		});
	}

	private saveCharts(): Promise<void> {
		const component = this.detailComponent?.chartListComponent;
		if(!component) {
			return Promise.resolve();
		}

		return this.entitySaveOrchestratorService.saveCharts(this.projectId, {
			chartManager: component.chartManager,
			charts: component.charts,
			originalCharts: component.originalCharts,
			modifiedChartIds: component.modifiedChartIds
		}).toPromise().then(() => {
			component.loadCharts();
			this.charts = this.chartManager.getAll();
		});
	}

	private confirmDiscardIfChanged(): Observable<boolean> {
		if(!this.hasModifications) {
			return of(true);
		}

		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			maxWidth: '90vw',
			data: {
				title: 'Discard Changes?',
				message: 'You have unsaved changes. Switching views will discard them. Continue?',
				confirmText: 'Discard',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});

		return dialogRef.afterClosed().pipe(
			map(confirmed => {
				if(confirmed) {
					this.performFullReset();
					return true;
				}
				return false;
			})
		);
	}

	onDiscardChanges(): void {
		this.confirmDiscardIfChanged().subscribe(confirmed => {
			if(confirmed) {
				this.snackBar.open('Changes discarded', 'Close', {duration: 2000});
			}
		});
	}

	private performFullReset(): void {
		this.workingProject = {...this.project!};
		this.modifiedFields.clear();

		this.scopeModelManager.resetToOriginals();
		this.eventModelManager.resetToOriginals();
		this.eventGroupManager.resetToOriginals();
		this.datasetModelManager.resetToOriginals();
		this.fieldModelManager.resetToOriginals();
		this.validatorManager.resetToOriginals();
		this.workflowManager.resetToOriginals();
		this.workflowStateManager.resetToOriginals();
		this.workflowActionManager.resetToOriginals();
		this.profileManager.resetToOriginals();
		this.featureManager.resetToOriginals();
		this.privacyPolicyManager.resetToOriginals();
		this.resourceCategoryManager.resetToOriginals();
		this.reportManager.resetToOriginals();
		this.chartManager.resetToOriginals();

		this.detailComponent?.scopeModelsListComponent?.loadScopeModels();
		this.detailComponent?.datasetModelsListComponent?.loadDatasetModels();
		this.detailComponent?.validatorsListComponent?.loadValidators();
		this.detailComponent?.workflowListComponent?.loadWorkflows();
		this.detailComponent?.profileListComponent?.loadProfiles();
		this.detailComponent?.featureListComponent?.loadFeatures();
		this.detailComponent?.privacyPolicyListComponent?.loadPrivacyPolicies();
		this.detailComponent?.resourceCategoryListComponent?.loadResourceCategories();
		this.detailComponent?.reportListComponent?.loadReports();
		this.detailComponent?.chartListComponent?.loadCharts();

		this.resetAllModifications();
		this.refreshTreeData();
	}

	resetAllModifications(): void {
		this.scopeModelModified = false;
		this.datasetModelModified = false;
		this.validatorModified = false;
		this.workflowModified = false;
		this.profileModified = false;
		this.featureModified = false;
		this.privacyPolicyModified = false;
		this.resourceCategoryModified = false;
		this.reportModified = false;
		this.chartModified = false;
	}

	onCreateSnapshot(): void {
		if(!this.draftVersion) {
			return;
		}

		this.snapshotManager.createSnapshot(this.projectId, this.draftVersion.pk!).subscribe({
			next: () => this.loadSnapshotState()
		});
	}

	onViewSnapshots(): void {
		if(!this.draftVersion) {
			return;
		}

		this.snapshotManager.viewSnapshots(this.projectId, this.draftVersion.pk!).subscribe({
			next: targetIndex => {
				if(targetIndex !== null) {
					this.restoreSnapshot(targetIndex);
				}
			}
		});
	}

	private restoreSnapshot(targetIndex: number): void {
		if(!this.draftVersion) {
			return;
		}

		this.snapshotManager.restoreToSnapshot(
			this.projectId,
			this.draftVersion.pk!,
			targetIndex
		).subscribe({
			next: updatedProject => {
				this.project = updatedProject;
				this.workingProject = {...updatedProject};
				this.modifiedFields.clear();
				this.loadSnapshotState();
			}
		});
	}

	onPublish(): void {
		console.log('Publish clicked');
	}

	onBack(): void {
		this.confirmDiscardIfChanged().subscribe(confirmed => {
			if(confirmed) {
				localStorage.removeItem('configProjectId');
				this.router.navigate(['/configurator']);
			}
		});
	}

	get hasModifications(): boolean {
		return this.projectModificationCount > 0
		  || this.scopeModelModified
		  || this.datasetModelModified
		  || this.validatorModified
		  || this.workflowModified
		  || this.profileModified
		  || this.featureModified
		  || this.privacyPolicyModified
		  || this.resourceCategoryModified
		  || this.reportModified
		  || this.chartModified;
	}

	onScopeModelContextChanged(context: any): void {
		setTimeout(() => {
			this.scopeModels = context.scopeModels;
			this.eventModels = context.eventModels;
			this.eventGroups = context.eventGroups;
			this.selectedScopeModelId = context.selectedScopeModelId;
			this.selectedEventModelId = context.selectedEventModelId;
			this.selectedEventGroupId = context.selectedEventGroupId;
		});
	}

	onDatasetModelContextChanged(context: any): void {
		setTimeout(() => {
			this.datasetModels = context.datasetModels;
			this.fieldModels = context.fieldModels;
			this.selectedDatasetModelId = context.selectedDatasetModelId;
			this.selectedFieldModelId = context.selectedFieldModelId;
		});
	}

	onValidatorContextChanged(context: any): void {
		setTimeout(() => {
			this.validators = context.validators;
			this.selectedValidatorId = context.selectedValidatorId;
		});
	}

	onWorkflowContextChanged(context: any): void {
		setTimeout(() => {
			this.workflows = context.workflows;
			this.workflowStates = context.workflowStates;
			this.workflowActions = context.workflowActions;
			this.selectedWorkflowId = context.selectedWorkflowId;
			this.selectedWorkflowStateId = context.selectedWorkflowStateId;
			this.selectedWorkflowActionId = context.selectedWorkflowActionId;
		});
	}

	onProfileContextChanged(context: any): void {
		setTimeout(() => {
			this.profiles = context.profiles;
			this.selectedProfileId = context.selectedProfileId;
		});
	}

	onFeatureContextChanged(context: any): void {
		setTimeout(() => {
			this.features = context.features;
			this.selectedFeatureId = context.selectedFeatureId;
		});
	}

	onPrivacyPolicyContextChanged(context: any): void {
		setTimeout(() => {
			this.privacyPolicies = context.privacyPolicies;
			this.selectedPrivacyPolicyId = context.selectedPrivacyPolicyId;
		});
	}

	onResourceCategoryContextChanged(context: any): void {
		setTimeout(() => {
			this.resourceCategories = context.resourceCategories;
			this.selectedResourceCategoryId = context.selectedResourceCategoryId;
		});
	}

	onReportContextChanged(context: any): void {
		setTimeout(() => {
			this.reports = context.reports;
			this.selectedReportId = context.selectedReportId;
		});
	}

	onChartContextChanged(context: any): void {
		setTimeout(() => {
			this.charts = context.charts;
			this.selectedChartId = context.selectedChartId;
		});
	}
}
