const prefix = 'application/vnd.rodanoconfig';

export const MediaTypes = Object.freeze({
	CLIPBOARD_ACTION: `${prefix}.clipboard.action`,

	NODE_ID: `${prefix}.node.id`,
	NODE_GLOBAL_ID: `${prefix}.node.global-id`,

	WIDGET_TYPE_ID: `${prefix}.layout.widget-type-id`,
	RULE_CONDITION_ID: `${prefix}.rule.condition-id`
});
