import {EntityVersion} from './entity-version';

export interface EntityAuditTrail extends EntityVersion {
	modifications: Record<string, {oldValue: string; newValue: string}>;
}
