import {Component, DestroyRef, Input, OnInit} from '@angular/core';
import {FormControl, FormGroup, Validators, ReactiveFormsModule} from '@angular/forms';
import {UserDTO} from '@core/model/user-dto';
import {ProfileDTO} from '@core/model/profile-dto';
import {ConfigurationService} from '@core/services/configuration.service';
import {switchMap} from 'rxjs/operators';
import {forkJoin, Observable} from 'rxjs';
import {RoleService} from '@core/services/role.service';
import {RoleDTO} from '@core/model/role-dto';
import {NotificationService} from 'src/app/services/notification.service';
import {GetFieldPipe} from '../../pipes/get-field.pipe';
import {LookupByIdPipe} from '../../pipes/lookup-by-id.pipe';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {MatIcon} from '@angular/material/icon';
import {MatOption} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatCardModule} from '@angular/material/card';
import {MatButton} from '@angular/material/button';
import {MatTableModule} from '@angular/material/table';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {AuditTrailButtonComponent} from 'src/app/audit-trail-button/audit-trail-button.component';
import {AuthStateService} from 'src/app/services/auth-state.service';
import {MeService} from '@core/services/me.service';
import {ArraySortPipe} from 'src/app/pipes/sort-array.pipe';
import {ScopeMiniDTO} from '@core/model/scope-mini-dto';
import {ScopePickerComponent} from 'src/app/scope-picker/scope-picker.component';
import {getRoleStatusDisplay} from '../role-status-display';
import {MatTooltip} from '@angular/material/tooltip';

@Component({
	templateUrl: './user-roles.component.html',
	styleUrls: ['./user-roles.component.css'],
	imports: [
		MatTableModule,
		MatButton,
		ReactiveFormsModule,
		MatCardModule,
		MatLabel,
		MatFormField,
		MatSelect,
		MatOption,
		MatIcon,
		LocalizeMapPipe,
		LookupByIdPipe,
		GetFieldPipe,
		ArraySortPipe,
		MatIcon,
		MatTooltip,
		AuditTrailButtonComponent,
		ScopePickerComponent
	]
})
export class UserRolesComponent implements OnInit {
	@Input() user: UserDTO;
	me: UserDTO;
	roles: RoleDTO[] = [];

	displayedColumns: string[] = [
		'profile',
		'scope',
		'status',
		'auditTrail',
		'actions'
	];

	profiles: ProfileDTO[];
	scopes: ScopeMiniDTO[];

	getRoleStatusDisplay = getRoleStatusDisplay;

	roleForm = new FormGroup({
		profile: new FormControl('', {
			nonNullable: true,
			validators: [Validators.required]
		}),
		scopePk: new FormControl(0, {
			nonNullable: true,
			validators: [Validators.required]
		})
	});

	constructor(
		private configurationService: ConfigurationService,
		private roleService: RoleService,
		private notificationService: NotificationService,
		private authStateService: AuthStateService,
		private destroyRef: DestroyRef,
		private meService: MeService) {}

	ngOnInit() {
		forkJoin({
			profiles: this.configurationService.getProfiles(),
			scopes: this.meService.getScopes(undefined, true, false),
			me: this.meService.get()
		}).subscribe(({profiles, scopes, me}) => {
			this.profiles = profiles;
			this.scopes = scopes;
			this.me = me;
			//initialize the table only when all data is available
			this.roles = this.user.roles;
		});
	}

	addRole() {
		const profileId = this.roleForm.controls.profile.value;
		const scopePk = this.roleForm.controls.scopePk.value;

		this.roleService.create(this.user.pk, profileId, scopePk).pipe(
			switchMap(() => this.roleService.getRoles(this.user.pk)),
			takeUntilDestroyed(this.destroyRef)
		).subscribe(roles => {
			this.roleForm.reset();
			this.roles = roles;
			this.notificationService.showSuccess('New role created');
		});
	}

	inviteToRole(rolePk: number) {
		this.performRoleAction(this.roleService.inviteToRole(this.user.pk, rolePk));
	}

	enableRole(rolePk: number) {
		this.performRoleAction(this.roleService.enableRole(this.user.pk, rolePk));
	}

	disableRole(rolePk: number) {
		this.performRoleAction(this.roleService.disableRole(this.user.pk, rolePk));
	}

	private performRoleAction(role$: Observable<RoleDTO>) {
		role$.pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: updatedRole => {
				const roleIndex = this.roles.findIndex(role => role.pk === updatedRole.pk);
				this.roles[roleIndex] = updatedRole;
				//Refresh the mat-table datasource
				this.roles = [...this.roles];
				this.user.roles = this.roles;
				this.authStateService.updateUser(this.user);
				this.notificationService.showSuccess('Role updated');
			},
			error: result => {
				this.notificationService.showError(`Unable to update role: ${result.error.message}`);
			}
		}
		);
	}
}
