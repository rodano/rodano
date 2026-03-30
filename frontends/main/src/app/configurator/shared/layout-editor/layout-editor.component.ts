import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatSnackBar} from '@angular/material/snack-bar';
import {CdkDragDrop, DragDropModule, moveItemInArray} from '@angular/cdk/drag-drop';
import {LanguageService} from '../../services/language.service';
import {ChartManagerService} from '../../services/manager/chart-manager.service';
import {WorkflowWidgetManagerService} from '../../services/manager/workflow-widget-manager.service';
import {WorkflowSummaryManagerService} from '../../services/manager/workflow-summary-manager.service';
import {ResourceCategoryManagerService} from '../../services/manager/resource-category-manager.service';
import {WidgetLayout} from '@core/model/widget-layout';
import {Section} from '@core/model/section';
import {WIDGET_TYPES, WidgetTypeDef} from '../layout-widget-types';
import {LayoutService} from '../../services/api/layout.service';
import {Widget} from '@core/model/widget';
import {LayoutDialogService} from '../../services/dialogs/layout-dialog.service';
import {ScopeModelManagerService} from '../../services/manager/scope-model-manager.service';
import {EventModelManagerService} from '../../services/manager/event-model-manager.service';
import {DatasetModelManagerService} from '../../services/manager/dataset-model-manager.service';
import {FormModelManagerService} from '../../services/manager/form-model-manager.service';
import {ProfileManagerService} from '../../services/manager/profile-manager.service';
import {FeatureManagerService} from '../../services/manager/feature-manager.service';

@Component({
	selector: 'app-layout-editor',
	standalone: true,
	templateUrl: './layout-editor.component.html',
	styleUrls: ['./layout-editor.component.css', '../list-shared.css'],
	imports: [CommonModule, FormsModule, MatIconModule, MatButtonModule, MatTooltipModule, DragDropModule]
})
export class LayoutEditorComponent implements OnInit, OnChanges {
	@Input() projectId = '';
	@Input() entityPath = '';
	@Output() closed = new EventEmitter<void>();

	layout: WidgetLayout = {sections: []};
	selectedSection: Section | null = null;
	loading = false;
	saving = false;
	modified = false;

	readonly widgetTypes = WIDGET_TYPES;

	constructor(
		public languageService: LanguageService,
		private layoutService: LayoutService,
		private layoutDialogService: LayoutDialogService,
		private chartManager: ChartManagerService,
		private featureManager: FeatureManagerService,
		private scopeModelManager: ScopeModelManagerService,
		private eventModelManager: EventModelManagerService,
		private datasetModelManager: DatasetModelManagerService,
		private formModelManager: FormModelManagerService,
		private profileManager: ProfileManagerService,
		private workflowWidgetManager: WorkflowWidgetManagerService,
		private workflowSummaryManager: WorkflowSummaryManagerService,
		private resourceCategoryManager: ResourceCategoryManagerService,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.loadLayout();
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['entityPath'] && !changes['entityPath'].firstChange) {
			this.loadLayout();
		}
	}

	loadLayout(): void {
		this.loading = true;
		const previousSectionId = this.selectedSection?.sectionId;
		this.selectedSection = null;
		this.layoutService.getLayout(this.projectId, this.entityPath).subscribe({
			next: layout => {
				this.layout = {sections: layout.sections ?? []};
				this.loading = false;
				this.modified = false;
				this.restoreSelection(previousSectionId);
			},
			error: () => {
				this.snackBar.open('Failed to load layout', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	onSave(): void {
		this.saving = true;
		const previousSectionId = this.selectedSection?.sectionId;
		this.layoutService.saveLayout(this.projectId, this.entityPath, this.layout).subscribe({
			next: saved => {
				this.layout = saved;
				this.saving = false;
				this.modified = false;
				this.snackBar.open('Layout saved', 'Close', {duration: 2000});
				this.restoreSelection(previousSectionId);
			},
			error: () => {
				this.snackBar.open('Failed to save layout', 'Close', {duration: 3000});
				this.saving = false;
			}
		});
	}

	private restoreSelection(previousSectionId: string | null | undefined): void {
		const sections = this.layout.sections ?? [];
		if(!sections.length) {
			this.selectedSection = null;
			return;
		}
		this.selectedSection = (previousSectionId
			? sections.find(s => s.sectionId === previousSectionId)
			: null) ?? sections[0];
	}

	onDiscard(): void {
		this.loadLayout();
	}

	addSection(): void {
		const newSection: Section = {
			id: '',
			label: {},
			sortOrder: (this.layout.sections ?? []).length,
			widgets: []
		};

		this.layoutDialogService.openSectionConfig(newSection).subscribe(result => {
			if(result) {
				const section: Section = {
					...newSection,
					id: result.id,
					label: result.label,
					requiredFeatureId: result.requiredFeatureId,
					rightEntity: result.rightEntity,
					rightValue: result.rightValue,
					rightTargetId: result.rightTargetId
				};
				this.layout = {...this.layout, sections: [...(this.layout.sections ?? []), section]};
				this.selectedSection = section;
				this.modified = true;
			}
		});
	}

	deleteSection(section: Section): void {
		this.layout = {...this.layout, sections: (this.layout.sections ?? []).filter(s => s !== section)};
		if(this.selectedSection === section) {
			this.selectedSection = null;
		}
		this.modified = true;
	}

	selectSection(section: Section): void {
		this.selectedSection = section;
	}

	dropSection(event: CdkDragDrop<Section[]>): void {
		const sections = [...(this.layout.sections ?? [])];
		moveItemInArray(sections, event.previousIndex, event.currentIndex);
		sections.forEach((s, i) => s.sortOrder = i);
		this.layout = {...this.layout, sections};
		this.modified = true;
	}

	getSectionLabel(section: Section): string {
		return this.languageService.getTranslatedName(section.label ?? {}) || section.id || '';
	}

	addWidget(type: WidgetTypeDef): void {
		if(!this.selectedSection) {
			return;
		}

		const widget: Widget = {
			type: type.type,
			width: 'FULL',
			widgetOrder: (this.selectedSection.widgets ?? []).length,
			parameters: {}
		};

		this.selectedSection.widgets = [...(this.selectedSection.widgets ?? []), widget];
		this.layout = {...this.layout};
		this.modified = true;

		if(type.parameters.length > 0) {
			this.openWidgetConfig(widget);
		}
	}

	deleteWidget(widget: Widget): void {
		if(!this.selectedSection) {
			return;
		}
		this.selectedSection.widgets = (this.selectedSection.widgets ?? []).filter(w => w !== widget);
		this.layout = {...this.layout};
		this.modified = true;
	}

	dropWidget(event: CdkDragDrop<Widget[]>): void {
		if(!this.selectedSection) {
			return;
		}
		const widgets = [...(this.selectedSection.widgets ?? [])];
		moveItemInArray(widgets, event.previousIndex, event.currentIndex);
		widgets.forEach((w, i) => w.widgetOrder = i);
		this.selectedSection.widgets = widgets;
		this.layout = {...this.layout};
		this.modified = true;
	}

	openSectionConfig(section: Section, event: Event): void {
		event.stopPropagation();
		this.layoutDialogService.openSectionConfig(section).subscribe(result => {
			if(result) {
				section.id = result.id;
				section.label = result.label;
				section.requiredFeatureId = result.requiredFeatureId;
				section.rightEntity = result.rightEntity;
				section.rightValue = result.rightValue;
				section.rightTargetId = result.rightTargetId;
				this.layout = {...this.layout};
				this.modified = true;
			}
		});
	}

	openWidgetConfig(widget: Widget): void {
		const typeDef = this.widgetTypes.find(t => t.type === widget.type);
		if(!typeDef) {
			return;
		}
		this.layoutDialogService.openWidgetConfig(widget, typeDef).subscribe(result => {
			if(result) {
				widget.parameters = result.parameters;
				widget.width = result.width;

				const optionalFields = ['textBefore', 'textAfter', 'requiredFeatureId', 'rightEntity', 'rightValue', 'rightTargetId'] as const;
				for(const field of optionalFields) {
					if(result[field]) {
						(widget as any)[field] = result[field];
					}
					else {
						delete (widget as any)[field];
					}
				}

				this.layout = {...this.layout};
				this.modified = true;
			}
		});
	}

	getWidgetLabel(widget: Widget): string {
		return this.widgetTypes.find(t => t.type === widget.type)?.label ?? widget.type ?? '';
	}

	getWidgetParamSummary(widget: Widget): string {
		const typeDef = this.widgetTypes.find(t => t.type === widget.type);
		if(!typeDef || typeDef.parameters.length === 0) {
			return '';
		}

		const parts: string[] = [];

		for(const param of typeDef.parameters) {
			const value = (widget.parameters ?? {})[param.id];

			if(param.kind === 'boolean') {
				if(value === 'true' || value === true as any) {
					parts.push(param.label);
				}
			}
			else if(value) {
				parts.push(this.resolveParamLabel(param.kind, value));
			}
		}

		return parts.join(' · ');
	}

	getFeatureTooltip(requiredFeatureId: string | null | undefined): string {
		if(!requiredFeatureId) {
			return '';
		}
		const feature = this.featureManager.getById(requiredFeatureId);
		return feature ? `Feature: ${this.languageService.getLabel(feature)}` : `Feature: ${requiredFeatureId}`;
	}

	getRightTooltip(rightEntity: string | null | undefined, rightValue: string | null | undefined, rightTargetId: string | null | undefined): string {
		if(!rightEntity) {
			return '';
		}
		const targetLabel = this.resolveRightTargetLabel(rightEntity, rightTargetId);
		const parts = [`Right: ${rightEntity.replace('_', ' ')}`];
		if(rightValue) {
			parts.push(rightValue);
		}
		if(targetLabel) {
			parts.push(`on ${targetLabel}`);
		}
		return parts.join(' · ');
	}

	getRightIcon(rightEntity: string | null | undefined): string {
		switch(rightEntity) {
			case 'SCOPE_MODEL': return 'account_tree';
			case 'EVENT_MODEL': return 'event';
			case 'DATASET_MODEL': return 'table_chart';
			case 'FORM_MODEL': return 'description';
			case 'PROFILE': return 'person';
			default: return 'lock';
		}
	}

	getRightThemeClass(rightEntity: string | null | undefined): string {
		switch(rightEntity) {
			case 'SCOPE_MODEL': return 'theme-scope-model';
			case 'EVENT_MODEL': return 'theme-event-model';
			case 'DATASET_MODEL': return 'theme-dataset-model';
			case 'FORM_MODEL': return 'theme-form-model';
			case 'PROFILE': return 'theme-profile';
			default: return 'theme-main';
		}
	}

	private resolveRightTargetLabel(rightEntity: string, rightTargetId: string | null | undefined): string {
		if(!rightTargetId) {
			return '';
		}
		switch(rightEntity) {
			case 'SCOPE_MODEL': {
				const e = this.scopeModelManager.getById(rightTargetId);
				return e ? this.languageService.getLabel(e) : rightTargetId;
			}
			case 'EVENT_MODEL': {
				const e = this.eventModelManager.getById(rightTargetId);
				return e ? this.languageService.getLabel(e) : rightTargetId;
			}
			case 'DATASET_MODEL': {
				const e = this.datasetModelManager.getById(rightTargetId);
				return e ? this.languageService.getLabel(e) : rightTargetId;
			}
			case 'FORM_MODEL': {
				const e = this.formModelManager.getById(rightTargetId);
				return e ? this.languageService.getLabel(e) : rightTargetId;
			}
			case 'PROFILE': {
				const e = this.profileManager.getById(rightTargetId);
				return e ? this.languageService.getLabel(e) : rightTargetId;
			}
			default: return rightTargetId;
		}
	}

	private resolveParamLabel(kind: string, value: string): string {
		switch(kind) {
			case 'chart': {
				const e = this.chartManager.getById(value);
				return e ? this.languageService.getLabel(e) : value;
			}
			case 'scopeModel': {
				const e = this.scopeModelManager.getById(value);
				return e ? this.languageService.getLabel(e) : value;
			}
			case 'workflowWidget': {
				const e = this.workflowWidgetManager.getById(value);
				return e ? this.languageService.getLabel(e) : value;
			}
			case 'workflowSummary': {
				const e = this.workflowSummaryManager.getById(value);
				return e ? this.languageService.getLabel(e) : value;
			}
			case 'resourceCategory': {
				const e = this.resourceCategoryManager.getById(value);
				return e ? this.languageService.getLabel(e) : value;
			}
			default: return value;
		}
	}
}
