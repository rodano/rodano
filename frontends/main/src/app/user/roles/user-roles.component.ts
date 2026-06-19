import {Component, DestroyRef, inject, OnInit, signal} from '@angular/core';
import {FormControl, FormGroup, Validators, ReactiveFormsModule} from '@angular/forms';
import {Profile} from '@core/model/profile';
import {ConfigurationService} from '@core/services/configuration.service';
import {switchMap} from 'rxjs/operators';
import {forkJoin, Observable} from 'rxjs';
import {RoleService} from '@core/services/role.service';
import {Role} from '@core/model/role';
import {NotificationService} from '../../services/notification.service';
import {GetFieldPipe} from '../../pipes/get-field.pipe';
import {LookupByIdPipe} from '../../pipes/lookup-by-id.pipe';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {MatIcon} from '@angular/material/icon';
import {MatOption} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatCardModule} from '@angular/material/card';
import {MatButton} from '@angular/material/button';
import {MatProgressBar} from '@angular/material/progress-bar';
import {MatTableModule} from '@angular/material/table';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {AuditTrailButtonComponent} from '../../audit-trail-button/audit-trail-button.component';
import {AuthStateService} from '../../services/auth-state.service';
import {MeService} from '@core/services/me.service';
import {ArraySortPipe} from '../../pipes/sort-array.pipe';
import {ScopeMini} from '@core/model/scope-mini';
import {ScopePickerComponent} from '../../scope-picker/scope-picker.component';
import {getRoleStatusDisplay} from '../role-status-display';
import {MatTooltip} from '@angular/material/tooltip';
import {USER_TOKEN} from '../home/user.component';

@Component({
	templateUrl: './user-roles.component.html',
	styleUrls: ['./user-roles.component.css'],
	imports: [
		MatTableModule,
		MatButton,
		ReactiveFormsModule,
		MatCardModule,
		MatProgressBar,
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
	readonly user = inject(USER_TOKEN);
	readonly loading = signal(false);

	displayedColumns: string[] = [
		'profile',
		'scope',
		'status',
		'auditTrail',
		'actions'
	];

	readonly profiles = signal<Profile[]>([]);
	readonly scopes = signal<ScopeMini[]>([]);

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
		this.loading.set(true);
		forkJoin({
			profiles: this.configurationService.getProfiles(),
			scopes: this.meService.getScopes(undefined, true, false)
		}).subscribe(({profiles, scopes}) => {
			this.profiles.set(profiles);
			this.scopes.set(scopes);
			this.loading.set(false);
		});
	}

	addRole() {
		const profileId = this.roleForm.controls.profile.value;
		const scopePk = this.roleForm.controls.scopePk.value;

		this.loading.set(true);
		this.roleService.create(this.user().pk, profileId, scopePk).pipe(
			switchMap(() => this.roleService.getRoles(this.user().pk)),
			takeUntilDestroyed(this.destroyRef)
		).subscribe(roles => {
			this.roleForm.reset();
			this.user().roles = roles;
			this.notificationService.showSuccess('New role created');
			this.loading.set(false);
		});
	}

	inviteToRole(rolePk: number) {
		this.performRoleAction(this.roleService.inviteToRole(this.user().pk, rolePk));
	}

	enableRole(rolePk: number) {
		this.performRoleAction(this.roleService.enableRole(this.user().pk, rolePk));
	}

	disableRole(rolePk: number) {
		this.performRoleAction(this.roleService.disableRole(this.user().pk, rolePk));
	}

	private performRoleAction(role$: Observable<Role>) {
		this.loading.set(true);
		role$.pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: updatedRole => {
				const roleIndex = this.user().roles.findIndex(r => r.pk === updatedRole.pk);
				this.user().roles[roleIndex] = updatedRole;
				this.user().roles = [...this.user().roles];
				this.authStateService.updateUser(this.user());
				this.notificationService.showSuccess('Role updated');
				this.loading.set(false);
			},
			error: result => {
				this.notificationService.showError(`Unable to update role: ${result.error.message}`);
				this.loading.set(false);
			}
		});
	}
}
