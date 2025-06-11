import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {APIService} from './api.service';
import {reviveDates} from '../decorators/revive-dates.decorator';
import {ScopeAuditTrail} from '../model/scope-audit-trail';
import {EventAuditTrail} from '../model/event-audit-trail';
import {DatasetAuditTrail} from '../model/dataset-audit-trail';
import {FormAuditTrail} from '../model/form-audit-trail';
import {FieldAuditTrail} from '../model/field-audit-trail';
import {WorkflowStatusAuditTrail} from '../model/workflow-status-audit-trail';
import {UserAuditTrailDTO} from '../model/user-audit-trail-dto';
import {RoleAuditTrail} from '../model/role-audit-trail';
import {RobotAuditTrail} from '../model/robot-audit-trail';

@Injectable({
	providedIn: 'root'
})
export class VersionsService {
	constructor(
		private http: HttpClient,
		private apiService: APIService
	) { }

	@reviveDates
	getForEntity<T>(url: string, auditActorPk?: number): Observable<T[]> {
		let params = new HttpParams();
		if(auditActorPk) {
			params = params.set('auditActorPk', auditActorPk.toString());
		}
		return this.http.get<T[]>(url, {params});
	}

	getForScope(scopePk: number, auditActorPk?: number): Observable<ScopeAuditTrail[]> {
		const url = `${this.apiService.getApiUrl()}/scopes/${scopePk}/versions`;
		return this.getForEntity<ScopeAuditTrail>(url, auditActorPk);
	}

	getForEvent(scopePk: number, eventPk: number, auditActorPk?: number): Observable<EventAuditTrail[]> {
		const url = `${this.apiService.getApiUrl()}/scopes/${scopePk}/events/${eventPk}/versions`;
		return this.getForEntity<EventAuditTrail>(url, auditActorPk);
	}

	getForDataset(scopePk: number, eventPk: number | undefined, datasetPk: number, auditActorPk?: number): Observable<DatasetAuditTrail[]> {
		let url = `${this.apiService.getApiUrl()}/scopes/${scopePk}`;
		if(eventPk) {
			url = `${url}/events/${eventPk}`;
		}
		return this.getForEntity<DatasetAuditTrail>(`${url}/datasets/${datasetPk}/versions`, auditActorPk);
	}

	getForForm(scopePk: number, eventPk: number | undefined, formPk: number, auditActorPk?: number): Observable<FormAuditTrail[]> {
		let url = `${this.apiService.getApiUrl()}/scopes/${scopePk}`;
		if(eventPk) {
			url = `${url}/events/${eventPk}`;
		}
		return this.getForEntity<FormAuditTrail>(`${url}/forms/${formPk}/versions`, auditActorPk);
	}

	getForField(scopePk: number, eventPk: number | undefined, datasetPk: number, fieldPk: number, auditActorPk?: number): Observable<FieldAuditTrail[]> {
		let url = `${this.apiService.getApiUrl()}/scopes/${scopePk}`;
		if(eventPk) {
			url = `${url}/events/${eventPk}`;
		}
		url = `${url}/datasets/${datasetPk}/fields/${fieldPk}/versions`;
		return this.getForEntity<FieldAuditTrail>(url, auditActorPk);
	}

	getForWorkflowStatus(workflowStatusPk: number, auditActorPk?: number): Observable<WorkflowStatusAuditTrail[]> {
		const url = `${this.apiService.getApiUrl()}/workflows/${workflowStatusPk}/versions`;
		return this.getForEntity<WorkflowStatusAuditTrail>(url, auditActorPk);
	}

	getForUser(userPk: number, auditActorPk?: number): Observable<UserAuditTrailDTO[]> {
		const url = `${this.apiService.getApiUrl()}/users/${userPk}/versions`;
		return this.getForEntity<UserAuditTrailDTO>(url, auditActorPk);
	}

	getForRobot(robotPk: number, auditActorPk?: number): Observable<UserAuditTrailDTO[]> {
		const url = `${this.apiService.getApiUrl()}/robots/${robotPk}/versions`;
		return this.getForEntity<RobotAuditTrail>(url, auditActorPk);
	}

	getForUserRole(userPk: number, rolePk: number, auditActorPk?: number): Observable<RoleAuditTrail[]> {
		const url = `${this.apiService.getApiUrl()}/users/${userPk}/roles/${rolePk}/versions`;
		return this.getForEntity<RoleAuditTrail>(url, auditActorPk);
	}

	getForRobotRole(robotPk: number, rolePk: number, auditActorPk?: number): Observable<RoleAuditTrail[]> {
		const url = `${this.apiService.getApiUrl()}/robots/${robotPk}/roles/${rolePk}/versions`;
		return this.getForEntity<RoleAuditTrail>(url, auditActorPk);
	}
}
