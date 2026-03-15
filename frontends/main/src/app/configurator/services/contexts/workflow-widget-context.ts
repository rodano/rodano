import {WorkflowWidgetManagerService} from '../manager/workflow-widget-manager.service';
import {WorkflowWidgetConfig} from '@core/model/workflow-widget-config';

export interface WorkflowWidgetContext {
	workflowWidgetManager: WorkflowWidgetManagerService;
	workflowWidgets: WorkflowWidgetConfig[];
	originalWorkflowWidgets: WorkflowWidgetConfig[];
	modifiedWorkflowWidgetIds: Set<string>;
}
