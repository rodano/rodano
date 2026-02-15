export interface TreeNode {
	id: string;
	label: string;
	icon: string;
	children?: TreeNode[];
	expanded?: boolean;
	selected?: boolean;
	type: 'scope-model' | 'event-model' | 'event-group' | 'dataset-model' | 'field-model' | 'validator';
	entityId: string;
}
