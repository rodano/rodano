export interface EntityVersion {
	pk: number;
	[otherProperties: string]: any;
	auditObjectFk: number;
	auditActionFk: number;
	auditActor: string;
	auditUserFk?: number;
	auditRobotFk?: number;
	auditDatetime: Date;
	auditContext: string;
}
