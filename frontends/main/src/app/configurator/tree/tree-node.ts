export interface TreeNode {
	id: string;
	label: string;
	icon: string;
	children?: TreeNode[];
	expanded?: boolean;
	selected?: boolean;
	type: 'scope-model' | 'event-model' | 'event-group' | 'dataset-model' | 'field-model' | 'validator' | 'workflow' | 'workflow-state' | 'workflow-action' | 'profile' | 'feature' | 'privacy-policy' | 'resource-category' | 'report' | 'chart' | 'form-model' | 'layout' | 'timeline-graph' | 'timeline-graph-section' ;
	entityId: string;
	themeClass?: string;
}
