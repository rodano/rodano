import {Component, DestroyRef, Input, OnInit} from '@angular/core';
import {ScopeDTO} from '@core/model/scope-dto';
import {UserDTO} from '@core/model/user-dto';
import {MatIcon} from '@angular/material/icon';
import {MatDialog} from '@angular/material/dialog';
import {AuditTrailPropertyComponent} from '../audit-trail-property/audit-trail-property.component';
import {MatIconButton} from '@angular/material/button';
import {MatTooltip} from '@angular/material/tooltip';
import {AuditTrailEntityComponent} from '../audit-trail-entity/audit-trail-entity.component';
import {AuditTrailService} from '@core/services/audit-trail.service';
import {RoleDTO} from '@core/model/role-dto';
import {Observable} from 'rxjs';
import {PropertyAuditTrail} from '@core/model/property-audit-trail';
import {RobotDTO} from '@core/model/robot-dto';
import {EventDTO} from '@core/model/event-dto';
import {DatasetDTO} from '@core/model/dataset-dto';
import {EntityAuditTrail} from '@core/model/entity-audit-trail';
import {LocalizeMapPipe} from '../pipes/localize-map.pipe';
import {FormDTO} from '@core/model/form-dto';
import {FieldDTO} from '@core/model/field-dto';
import {AuthStateService} from '../services/auth-state.service';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FeatureStatic} from '@core/model/feature-static';

@Component({
	selector: 'app-audit-trail-button',
	templateUrl: './audit-trail-button.component.html',
	styleUrls: ['./audit-trail-button.component.css'],
	providers: [LocalizeMapPipe],
	imports: [
		MatIconButton,
		MatIcon,
		MatTooltip
	]
})
export class AuditTrailButtonComponent implements OnInit {
	hasRight = false;
	@Input() scope: ScopeDTO;
	@Input() event: EventDTO;
	@Input() dataset: DatasetDTO;
	@Input() form: FormDTO;
	@Input() field: FieldDTO;
	@Input() user: UserDTO;
	@Input() robot: RobotDTO;
	@Input() role: RoleDTO;
	@Input() property: string;

	constructor(
		private authStateService: AuthStateService,
		private auditTrailService: AuditTrailService,
		private destroyRef: DestroyRef,
		private localizeMapPipe: LocalizeMapPipe,
		private dialog: MatDialog
	) { }

	ngOnInit(): void {
		this.authStateService.listenConnectedUser().pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(user => {
			this.hasRight = user?.roles.some(r => r.profile.features.includes(FeatureStatic.VIEW_AUDIT_TRAIL)) ?? false;
		});
	}

	private getEntityName(): string {
		if(this.scope) {
			return this.scope.shortname;
		}
		if(this.event) {
			return this.event.shortname;
		}
		if(this.dataset) {
			return this.localizeMapPipe.transform(this.dataset.model.shortname);
		}
		if(this.form) {
			return this.localizeMapPipe.transform(this.form.model.shortname);
		}
		if(this.user) {
			return this.user.name;
		}
		if(this.robot) {
			return this.robot.name;
		}
		if(this.role) {
			return 'Role';
		}
		return 'Entity';
	}

	private getEntityTrails(): Observable<EntityAuditTrail[]> {
		if(this.scope) {
			return this.auditTrailService.getForScope(this.scope.pk);
		}
		if(this.event) {
			return this.auditTrailService.getForEvent(this.event.scopePk, this.event.pk);
		}
		if(this.dataset) {
			return this.auditTrailService.getForDataset(this.dataset.scopePk, this.dataset.eventPk, this.dataset.pk);
		}
		if(this.form) {
			return this.auditTrailService.getForForm(this.form.scopePk, this.form.eventPk, this.form.pk);
		}
		return this.auditTrailService.getForUser(this.user.pk);
	}

	private getPropertyTrails(): Observable<PropertyAuditTrail[]> {
		if(this.scope) {
			return this.auditTrailService.getForScopeProperty(this.scope.pk, this.property);
		}
		if(this.user) {
			return this.auditTrailService.getForUserProperty(this.user.pk, this.property);
		}
		if(this.robot) {
			return this.auditTrailService.getForRobotProperty(this.robot.pk, this.property);
		}
		return this.auditTrailService.getForRoleProperty(this.role, this.property);
	}

	openModal() {
		//open the right modal based on the provided parameters
		if(this.property) {
			return this.getPropertyTrails().subscribe(trails => {
				const data = {
					entityName: this.getEntityName(),
					property: this.property,
					trails
				};
				return this.dialog
					.open(AuditTrailPropertyComponent, {data})
					.afterClosed();
			});
		}
		return this.getEntityTrails().subscribe(trails => {
			const data = {
				entityName: this.getEntityName(),
				trails
			};
			return this.dialog
				.open(AuditTrailEntityComponent, {data})
				.afterClosed();
		});
	}
}
