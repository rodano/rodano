import {PropertyAuditTrail} from './property-audit-trail';

export type FieldEventEntityType = 'FIELD' | 'WORKFLOW_STATUS';

export const FieldEventEntityType = {
	FIELD: 'FIELD' as FieldEventEntityType,
	WORKFLOW_STATUS: 'WORKFLOW_STATUS' as FieldEventEntityType
};

export interface FieldEventAuditTrail extends PropertyAuditTrail {
	name: string;
	entityType: FieldEventEntityType;
	entityPk: number;
}
