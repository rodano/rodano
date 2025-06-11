import {Injectable} from '@angular/core';
import {forkJoin, map, Observable} from 'rxjs';
import {FieldDTO} from '../model/field-dto';
import {VersionsService} from './versions.service';
import {PropertyAuditTrail} from '../model/property-audit-trail';
import {EntityVersion} from '../model/entity-version';
import {EntityAuditTrail} from '../model/entity-audit-trail';
import {DateTimeUTCPipe} from 'src/app/pipes/date-time-utc.pipe';
import {RoleDTO} from '../model/role-dto';
import {FieldEventAuditTrail, FieldEventEntityType} from '../model/field-event-audit-trail';
import {LocalizeMapPipe} from 'src/app/pipes/localize-map.pipe';

@Injectable({
	providedIn: 'root'
})
export class AuditTrailService {
	dateTimeUTCPipe: DateTimeUTCPipe;
	localizeMapPipe: LocalizeMapPipe;

	constructor(
		private versionsService: VersionsService
	) {
		this.dateTimeUTCPipe = new DateTimeUTCPipe();
		this.localizeMapPipe = new LocalizeMapPipe();
	}

	private valuesEqual(v1: any, v2: any): boolean {
		if((v1 && !v2) || (!v1 && v2)) {
			return false;
		}
		if(v1 instanceof Date && v2 instanceof Date) {
			return v1.getTime() === v2.getTime();
		}
		if(v1 instanceof Object && v2 instanceof Object) {
			return JSON.stringify(v1) === JSON.stringify(v2);
		}
		return v1 === v2;
	}

	private stringify(value: any): string {
		if(!value) {
			return '';
		}
		if(value instanceof Date) {
			return this.dateTimeUTCPipe.transform(value);
		}
		if(value instanceof Object) {
			return JSON.stringify(value);
		}
		return value.toString();
	}

	private generateEntityAuditTrail(versions: EntityVersion[]) {
		const trails = [] as EntityAuditTrail[];
		if(versions.length > 0) {
			//retrieve all the interesting properties of the entity
			const entityProperties = Object.keys(versions[0]).filter(key => !key.startsWith('audit') && key !== 'pk');

			versions.forEach((version, index) => {
				const modifications = {} as Record<string, {oldValue: string; newValue: string}>;
				entityProperties.forEach(property => {
					const previousVersion = index === 0 ? undefined : versions[index - 1];
					const previousValue = previousVersion?.[property];
					const value = version[property] as string;
					if(!this.valuesEqual(previousValue, value)) {
						modifications[property] = {oldValue: this.stringify(previousValue), newValue: this.stringify(value)};
					}
				});

				trails.push({
					...version,
					modifications
				});
			});
		}

		//sort trails from latest to oldest
		//do not try to sort versions at the beginning of this method, because we need them to be in chronological order to build the list of trail properly
		trails.sort((t1, t2) => t2.auditDatetime.getTime() - t1.auditDatetime.getTime());

		return trails;
	}

	private generatePropertyAuditTrail(versions: EntityVersion[], property: string): PropertyAuditTrail[] {
		const trails = [] as PropertyAuditTrail[];

		if(versions.length > 0) {
			versions.forEach((version, index) => {
				const previousValue = index === 0 ? undefined : (versions[index - 1][property]) as string;
				const value = version[property] as string;
				if(!this.valuesEqual(previousValue, value)) {
					trails.push({
						value: this.stringify(value),
						by: version.auditActor,
						date: version.auditDatetime,
						context: version.auditContext
					});
				}
			});

			//sort trails from latest to oldest
			//do not try to sort versions at the beginning of this method, because we need them to be in chronological order to build the list of trail properly
			trails.sort((t1, t2) => t2.date.getTime() - t1.date.getTime());
		}

		return trails;
	}

	getForScope(scopePk: number): Observable<EntityAuditTrail[]> {
		return this.versionsService.getForScope(scopePk).pipe(
			map(v => this.generateEntityAuditTrail(v as EntityVersion[]))
		);
	}

	getForScopeProperty(scopePk: number, property: string): Observable<PropertyAuditTrail[]> {
		return this.versionsService.getForScope(scopePk).pipe(
			map(v => this.generatePropertyAuditTrail(v as EntityVersion[], property))
		);
	}

	getForEvent(scopePk: number, eventPk: number): Observable<EntityAuditTrail[]> {
		return this.versionsService.getForEvent(scopePk, eventPk).pipe(
			map(v => this.generateEntityAuditTrail(v as EntityVersion[]))
		);
	}

	getForDataset(scopePk: number, eventPk: number | undefined, datasetPk: number): Observable<EntityAuditTrail[]> {
		return this.versionsService.getForDataset(scopePk, eventPk, datasetPk).pipe(
			map(v => this.generateEntityAuditTrail(v as EntityVersion[]))
		);
	}

	getForForm(scopePk: number, eventPk: number | undefined, datasetPk: number): Observable<EntityAuditTrail[]> {
		return this.versionsService.getForForm(scopePk, eventPk, datasetPk).pipe(
			map(v => this.generateEntityAuditTrail(v as EntityVersion[]))
		);
	}

	getForUser(userPk: number): Observable<EntityAuditTrail[]> {
		return this.versionsService.getForUser(userPk).pipe(
			map(v => this.generateEntityAuditTrail(v as EntityVersion[]))
		);
	}

	getForUserProperty(userPk: number, property: string): Observable<PropertyAuditTrail[]> {
		return this.versionsService.getForUser(userPk).pipe(
			map(v => this.generatePropertyAuditTrail(v as EntityVersion[], property))
		);
	}

	getForRobot(robotPk: number): Observable<EntityAuditTrail[]> {
		return this.versionsService.getForRobot(robotPk).pipe(
			map(v => this.generateEntityAuditTrail(v as EntityVersion[]))
		);
	}

	getForRobotProperty(robotPk: number, property: string): Observable<PropertyAuditTrail[]> {
		return this.versionsService.getForRobot(robotPk).pipe(
			map(v => this.generatePropertyAuditTrail(v as EntityVersion[], property))
		);
	}

	getForRoleProperty(role: RoleDTO, property: string) {
		return role.userPk
			? this.getForUserRoleProperty(role.userPk, role.pk, property)
			: this.getForUserRoleProperty(role.robotPk as number, role.pk, property);
	}

	getForUserRoleProperty(userPk: number, rolePk: number, property: string): Observable<PropertyAuditTrail[]> {
		return this.versionsService.getForUserRole(userPk, rolePk).pipe(
			map(v => this.generatePropertyAuditTrail(v as EntityVersion[], property))
		);
	}

	getForRobotRoleProperty(robotPk: number, rolePk: number, property: string): Observable<PropertyAuditTrail[]> {
		return this.versionsService.getForRobotRole(robotPk, rolePk).pipe(
			map(v => this.generatePropertyAuditTrail(v as EntityVersion[], property))
		);
	}

	getForField(field: FieldDTO): Observable<FieldEventAuditTrail[]> {
		const statuses = field.workflowStatuses;
		return forkJoin([
			this.versionsService.getForField(field.scopePk, field.eventPk, field.datasetPk, field.pk),
			...statuses.map(w => this.versionsService.getForWorkflowStatus(w.pk))
		]).pipe(map(result => {
			const trails = [] as FieldEventAuditTrail[];
			trails.push(
				...this.generatePropertyAuditTrail(result[0] as EntityVersion[], 'value')
					.map(p => ({...p, name: 'Value change', entityType: FieldEventEntityType.FIELD, entityPk: field.pk}))
			);
			const statusTrails = result.slice(1) as EntityVersion[][];
			statusTrails.forEach((versions, index: number) => {
				const status = statuses[index];
				const statusLabel = this.localizeMapPipe.transform(status.workflow.shortname);
				trails.push(
					...this.generatePropertyAuditTrail(versions, 'stateId')
						.map(p => ({...p, name: statusLabel, entityType: FieldEventEntityType.WORKFLOW_STATUS, entityPk: status.pk}))
				);
			});
			trails.sort((t1, t2) => t2.date.getTime() - t1.date.getTime());
			return trails;
		}));
	}
}
