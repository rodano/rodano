import {Component, EventEmitter, OnInit, Output, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ChartDTO, printChartDTO} from '@core/model/chart-dto';
import {MatIcon} from '@angular/material/icon';
import {MatButton} from '@angular/material/button';
import {FormsModule} from '@angular/forms';
import {DynamicFormComponent} from '../ui-components/dynamic-form/dynamic-form.component';
import {
	CHART_TYPE_TO_GRAPH_TYPE, CHART_TYPE_TO_REQUEST_PARAMS,
	ChartType, DataLabelFormats,
	DataLabelPositions,
	GraphType, RequestParameters,
	UnitFormats
} from '../../widgets/chart/chartType';
import {ConfigurationService} from '@core/services/configuration.service';
import {
	ChartUiComponent
} from '../../widgets/chart/chart-ui/chart-ui.component';
import {NgClass} from '@angular/common';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {MatDialog} from '@angular/material/dialog';
import {DialogComponent, DialogData} from '../ui-components/dialog/dialog.component';
import {ChartService} from '@core/services/chart.service';
import {NotificationService} from '../../services/notification.service';
import {LoggingService} from '@core/services/logging.service';

@Component({
	selector: 'app-configuration-charts',
	imports: [
		MatIcon,
		MatButton,
		FormsModule,
		DynamicFormComponent,
		ChartUiComponent,
		NgClass
	],
	templateUrl: './configuration-charts.component.html',
	styleUrl: './configuration-charts.component.css',
	animations: [
		trigger('fadeInOut', [
			state('void', style({opacity: 0})),
			transition(':enter', [animate('300ms ease-in')]),
			transition(':leave', [animate('300ms ease-out')])
		])
	]
})
export class ConfigurationChartsComponent implements OnInit {
	/**Emits when the chart is deleted, passing the chart ID*/
	@Output() chartDeleted = new EventEmitter<string>();

	/**pointer to chart inside preview to manually trigger re-draw if needed*/
	@ViewChild(ChartUiComponent) chartUiComponent: ChartUiComponent;

	chartId: string;
	chart: ChartDTO | undefined;

	/**Used to check if chart configuration has been edited*/
	originalChart: ChartDTO | undefined;

	/**Flag used to enable/disable the save button*/
	changesMade = false;

	/**Flag for hiding/showing the 'reload data' button*/
	showSyncButton = false;

	/**Flag to know if the chart to save is new*/
	isNewChart = false;

	//Visual Dropdown options
	/**The chart_type dropdown options formatted in a user-friendly way**/
	chartTypeOptions = Object.values(ChartType).map(type => ({
		value: type,
		label: type
			.toLowerCase()
			.replace(/_/g, ' ')
			.replace(/\b\w/g, char => char.toUpperCase())
	}));

	/**Holds the graph_type dropdown options*/
	graphTypeOptions: GraphType[] = [];

	/**Holds the unit_format dropdown options*/
	unitFormatOptions = Object.values(UnitFormats);

	/**Holds the dataLabelPos dropdown options*/
	dataLabelPositions = Object.values(DataLabelPositions);

	/**Holds the dataLabelFormat dropdown options*/
	dataLabelFormats = Object.values(DataLabelFormats);

	//Request Parameters
	/**Holds id, shortname, datasets and events of all possible scopes**/
	scopeModels: {
		id: string;
		shortname: string;
		datasetModelIds: string[];
		events: {
			id: string;
			shortname: string;
			datasetModelIds: string[];
		}[];
	}[] = [];

	/**Holds id, shortname and fields of all possible datasets**/
	datasetModels: {
		id: string;
		shortname: string;
		fields: {id: string; shortname: string}[];
	}[] = [];

	/**Holds id and shortname of all possible fields**/
	fieldModels: {id: string; shortname: string}[] = [];

	/**Holds id, shortname and states of all possible workflows**/
	workflows: {
		id: string;
		shortname: string;
		states: {id: string; shortname: string}[];
	}[] = [];

	/**The currently selectable states**/
	currentStates: {id: string; shortname: string}[] = [];

	/**The currently selectable datasets**/
	currentDatasets: {id: string; shortname: string}[] = [];

	/**The currently selectable fields**/
	currentFields: {id: string; shortname: string}[] = [];

	/**The currently selectable events**/
	currentEvents: {id: string; shortname: string}[] = [];

	constructor(
		private route: ActivatedRoute,
		private chartService: ChartService,
		private configurationService: ConfigurationService,
		private dialog: MatDialog,
		private router: Router,
		private notificationService: NotificationService,
		private loggingService: LoggingService
	) {}

	/**
		* Fetch chart ID from the route and initialize chart data.
		*/
	ngOnInit() {
		//Retrieve chartId from route parameters
		this.route.paramMap.subscribe(params => {
			const newChartId = params.get('chartId') || '';
			if(newChartId !== this.chartId) {
				this.chartId = newChartId;
				this.loadChart();
			}
		});

		this.route.queryParamMap.subscribe(params => {
			this.isNewChart = params.get('new') === 'true';
		});
	}

	/**
		* Loads chart by ID and sets visual and validation flags.
		*/
	loadChart() {
		this.chartService.getChart(this.chartId).subscribe({
			next: (chart: ChartDTO) => {
				this.originalChart = chart;
				this.chart = JSON.parse(JSON.stringify(this.originalChart));
				this.showSyncButton = false;
				if(this.chart) {
					this.graphTypeOptions = CHART_TYPE_TO_GRAPH_TYPE[this.chart.chartType];
					this.loadConfigurationData();
					printChartDTO(this.chart);
				}
			},
			error: err => {
				this.notificationService.showError(err);
				this.loggingService.error(err);
			}
		});
	}

	/**
		* Loads scopes, datasets, fields, workflows, states and events from the configuration
		*/
	loadConfigurationData() {
		this.configurationService.getScopeModels().subscribe(data => {
			this.scopeModels = data.map(scopeModel => ({
				id: scopeModel.id,
				shortname: scopeModel.shortname.en,
				datasetModelIds: scopeModel.datasetModelIds,
				events: scopeModel.eventModels?.map(event => ({
					id: event.id,
					shortname: event.shortname.en,
					datasetModelIds: event.datasetModelIds
				}))
			}));

			if(this.chart?.requestParams.leafScopeModelId) {
				this.updateEventsBasedOnSelectedScope(this.chart.requestParams.leafScopeModelId);
			}
		});

		this.configurationService.getDatasetModels().subscribe(data => {
			this.datasetModels = data.map(datasetModel => ({
				id: datasetModel.id,
				shortname: datasetModel.shortname.en,
				fields: datasetModel.fieldModels.map(field => ({
					id: field.id,
					shortname: field.shortname.en
				}))
			}));

			if(this.chart?.requestParams.eventModelId) {
				this.updateDatasetBasedOnSelectedEvent(this.chart.requestParams.eventModelId);
			}
			else if(this.chart?.requestParams.leafScopeModelId) {
				this.updateDatasetBasedOnSelectedScope(this.chart.requestParams.leafScopeModelId);
			}

			if(this.chart?.requestParams.datasetModelId) {
				this.updateFieldsBasedOnSelectedDataset(this.chart?.requestParams.datasetModelId);
			}
		});

		this.configurationService.getSearchableFieldModels().subscribe(data => {
			this.fieldModels = data.map(fieldModel => ({
				id: fieldModel.id,
				shortname: fieldModel.shortname.en
			}));
		});

		this.configurationService.getWorkflows().subscribe(data => {
			this.workflows = data.map(workflow => ({
				id: workflow.id,
				shortname: workflow.shortname.en,
				states: workflow.states.map(state => ({
					id: state.id,
					shortname: state.shortname.en
				}))
			}));

			if(this.chart?.requestParams.workflowId) {
				this.updateStatesBasedOnSelectedWorkflow(this.chart?.requestParams.workflowId);
			}
		});
	}

	/**
		* Called on field changes. Updates sync and validation flags accordingly.
		* @param key The field that changed.
		* @param value The new value.
		*/
	handleFieldChange(key: string, value: any): void {
		//When the chart-type changed, the changeable request parameters need to change accordingly
		if(key.endsWith('chartType') && this.chart) {
			this.graphTypeOptions = CHART_TYPE_TO_GRAPH_TYPE[value as ChartType];
		}

		//When the workflowId changed, the selectable states need to change accordingly
		if(key.endsWith('workflowId') && this.chart) {
			this.updateStatesBasedOnSelectedWorkflow(this.chart?.requestParams.workflowId);
			this.chart.requestParams.stateIds = [];
		}

		//When the datasetModelId changed, the selectable fieldModelIds need to change accordingly
		if(key.endsWith('datasetModelId') && this.chart) {
			this.updateFieldsBasedOnSelectedDataset(this.chart?.requestParams.datasetModelId);
			this.chart.requestParams.fieldModelId = null;
		}

		//When the leafScopeModelId changed, the selectable datasets need to change accordingly
		if(key.endsWith('leafScopeModelId') && this.chart) {
			this.updateDatasetBasedOnSelectedScope(this.chart?.requestParams.leafScopeModelId);
			this.updateEventsBasedOnSelectedScope(this.chart?.requestParams.leafScopeModelId);
			this.chart.requestParams.datasetModelId = null;
			this.chart.requestParams.fieldModelId = null;
			this.currentFields = [];
		}

		if(key.endsWith('eventModelId') && this.chart) {
			this.updateDatasetBasedOnSelectedEvent(this.chart.requestParams.eventModelId);
			this.chart.requestParams.datasetModelId = null;
			this.chart.requestParams.fieldModelId = null;
			this.currentFields = [];
		}

		//Set the sync button visible when request parameters change so the user can try loading new data
		if(Object.values(RequestParameters).includes(key as RequestParameters)) {
			this.showSyncButton = true;
		}
		this.checkForChanges();

		//We don't need to re-draw the chart automatically when requestParams change
		if(!key.includes('requestParams') && this.chartUiComponent) {
			this.chartUiComponent.updateChartManually();
		}
	}

	/**
		* Checks if the user made any changes to the current chart configuration so far
		*/
	checkForChanges() {
		this.changesMade = JSON.stringify(this.chart) !== JSON.stringify(this.originalChart);
	}

	/**
		* Handler for the case that the "save" button is pressed. Either saves a changed chart or saves a new one.
		*/
	handleSave() {
		if(this.chart) {
			if(this.isNewChart) {
				this.chartService.createChart(this.chart).subscribe({
					next: (chart: ChartDTO) => {
						//When we save, this is now the new original chart
						this.originalChart = chart;
						this.isNewChart = false;
						this.showSyncButton = false;
						this.changesMade = false;
						//Chart now saved in DB, can be deleted as a temporary one in frontend
						this.chartService.clearTemporaryChart();
						this.notificationService.showSuccess('Successfully created new chart!');
					},
					error: err => {
						this.notificationService.showError(err);
						this.loggingService.error(err);
					}
				});
			}
			else {
				this.chartService.updateChart(this.chart).subscribe({
					next: (chart: ChartDTO) => {
						this.originalChart = chart;
						this.showSyncButton = false;
						this.changesMade = false;
						this.notificationService.showSuccess('Changes saved successfully!');
					},
					error: err => {
						this.notificationService.showError(err);
						this.loggingService.error(err);
					}
				});
			}
		}
	}

	/**
		* When the user cancels the editing process, the chart representation reverts.
		*/
	handleCancel() {
		this.chart = JSON.parse(JSON.stringify(this.originalChart));
		this.changesMade = false;
		this.showSyncButton = false;
		this.chartService.clearTemporaryChart();
	}

	/**
		* Deletes the current chart
		*/
	handleDelete() {
		this.chartService.deleteChart(this.chartId).subscribe({
			next: () => {
				this.router.navigate(['/configuration', 'chart']);
				this.chartDeleted.emit(this.chartId);
			},
			error: err => {
				this.notificationService.showError(err);
				this.loggingService.error(err);
			}
		});
	}

	/**
		* When the user changes Request Parameters, a sync button should appear allowing the user to re-load the data
		* with the new parameters.
		*/
	handleSync() {
		if(this.chart) {
			this.chartService.getData(this.chart.chartType, this.chart.requestParams).subscribe({
				next: data => {
					if(this.chart) {
						this.chart.data = data;
					}
					if(this.chartUiComponent) {
						this.chartUiComponent.updateChartManually();
					}
				},
				error: err => {
					this.notificationService.showError(err);
					this.loggingService.error(err);
				}
			});
		}

		this.showSyncButton = false;
	}

	/**
		* Generates all excluded fields based on chartType, so the user only sees the fields that actually matter.
		* @param chartType
		*/
	getExcludedFields(chartType: ChartType): string[] {
		const mapping = CHART_TYPE_TO_REQUEST_PARAMS[chartType];
		if(!mapping) {
			console.warn(`Unknown chart type: ${chartType}`);
		}

		const requiredParams = new Set(
			Array.isArray(mapping) ? mapping.map(p => p.toString()) : []
		);

		const allParams = Object.values(RequestParameters);
		const excludedParams = allParams
			.filter(param => !requiredParams.has(param))
			.map(param => `requestParams.${param}`);

		//If WorkflowId is not set, we don't need to display StateIds
		if(!this.chart?.requestParams.workflowId) {
			excludedParams.push(`requestParams.${RequestParameters.STATE_IDS}`);
		}

		return [
			'data', //The data itself is always excluded when working on the chart configuration
			...excludedParams
		];
	}

	/**
		* Opens a confirmation dialog before deleting a chart.
		*/
	openDeleteConfirmationDialog(): void {
		const data: DialogData = {
			title: `Deleting chart: ${this.chart?.chartId}`,
			message: 'Are you sure you want to delete this chart? Please choose how to proceed',
			buttons: [
				{label: 'Continue Editing', icon: '', value: 'continue'},
				{label: 'Delete chart', icon: '', value: 'delete'}
			]
		};

		this.dialog.open(DialogComponent, {data, width: '400px', disableClose: true
		}).afterClosed().subscribe(result => {
			switch(result) {
				case 'continue':
					//Do nothing and continue editing
					break;
				case 'delete':
					this.handleDelete();
					break;
				default:
					//Do nothing
					break;
			}
		});
	}

	/**
	 * When the user selects another workflow, the selectable states need to adjust accordingly
	 * @param workflowId
	 */
	updateStatesBasedOnSelectedWorkflow(workflowId: string | null): void {
		if(!workflowId) {
			this.currentStates = [];
			return;
		}

		const workflow = this.workflows.find(w => w.id === workflowId);

		this.currentStates = workflow?.states.map(state => ({
			id: state.id,
			shortname: state.shortname
		})) ?? [];
	}

	/**
	 * When the user selects another dataset, the selectable fields need to adjust accordingly
	 * @param datasetModelId
	 */
	updateFieldsBasedOnSelectedDataset(datasetModelId: string | null): void {
		if(!datasetModelId) {
			this.currentFields = [];
			return;
		}

		const datasetModel = this.datasetModels.find(d => d.id === datasetModelId);

		this.currentFields = datasetModel?.fields.map(field => ({
			id: field.id,
			shortname: field.shortname
		})) ?? [];
	}

	/**
	 * When the user selects another scope, the selectable datasets need to adjust accordingly
	 * @param scopeModelId
	 */
	updateDatasetBasedOnSelectedScope(scopeModelId: string | null): void {
		if(!scopeModelId) {
			this.currentDatasets = [];
			return;
		}

		const scope = this.scopeModels.find(s => s.id === scopeModelId);

		if(!scope) {
			this.currentDatasets = [];
			return;
		}

		this.currentDatasets = this.datasetModels.filter(ds =>
			scope.datasetModelIds.includes(ds.id)
		) ?? [];
	}

	/**
	 * When the user selects another scope, the selectable events need to change accordingly
	 * @param scopeModelId
	 */
	updateEventsBasedOnSelectedScope(scopeModelId: string | null): void {
		if(!scopeModelId) {
			this.currentEvents = [];
			return;
		}

		const scope = this.scopeModels.find(s => s.id === scopeModelId);

		if(!scope) {
			this.currentEvents = [];
			return;
		}

		this.currentEvents = scope.events ?? [];
	}

	updateDatasetBasedOnSelectedEvent(eventModelId: string | null): void {
		if(!eventModelId) {
			this.currentDatasets = [];
			return;
		}

		//Find the scope that contains the selected event
		const scopeWithEvent = this.scopeModels.find(scope =>
			scope.events.some(event => event.id === eventModelId)
		);

		if(!scopeWithEvent) {
			this.currentDatasets = [];
			return;
		}

		const event = scopeWithEvent.events.find(e => e.id === eventModelId);
		if(!event) {
			this.currentDatasets = [];
			return;
		}

		this.currentDatasets = this.datasetModels.filter(ds =>
			event.datasetModelIds.includes(ds.id)
		);
	}

	get scopeModelOptions() {
		return this.toOptionList(this.scopeModels);
	}

	get datasetModelOptions() {
		return this.toOptionList(this.currentDatasets);
	}

	get fieldModelOptions() {
		return this.toOptionList(this.currentFields);
	}

	get workflowOptions() {
		return this.toOptionList(this.workflows);
	}

	get stateOptions() {
		return this.toOptionList(this.currentStates);
	}

	get eventOptions() {
		return this.toOptionList(this.currentEvents);
	}

	private toOptionList<T extends {id: string; shortname: string}>(items: T[]): {label: string; value: string}[] {
		return items.map(item => ({
			label: item.shortname,
			value: item.id
		}));
	}
}
