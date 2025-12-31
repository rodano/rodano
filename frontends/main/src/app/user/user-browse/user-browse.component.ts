import {Component, DestroyRef, OnInit} from '@angular/core';
import {Profile} from '@core/model/profile';
import {ConfigurationService} from '@core/services/configuration.service';
import {UserSearch} from '@core/utilities/search/user-search';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {MatIcon} from '@angular/material/icon';
import {RouterLink} from '@angular/router';
import {MatButton} from '@angular/material/button';
import {MatOption} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';
import {MatFormField, MatInput, MatLabel} from '@angular/material/input';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {UserListComponent} from '../user-list/user-list.component';
import {ScopePickerComponent} from 'src/app/scope-picker/scope-picker.component';
import {ScopeMini} from '@core/model/scope-mini';
import {MeService} from '@core/services/me.service';
import {forkJoin, Observable} from 'rxjs';
import {User} from '@core/model/user';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {RoleStatus} from '@core/model/role-status';
import {getRoleStatusDisplay} from '../role-status-display';
import {FeatureStatic} from '@core/model/feature-static';
import {PermissionsService} from '@core/services/permission.service';
import {AsyncPipe} from '@angular/common';

@Component({
	templateUrl: './user-browse.component.html',
	styleUrls: ['./user-browse.component.css'],
	imports: [
		ReactiveFormsModule,
		MatLabel,
		MatFormField,
		MatInput,
		MatSelect,
		MatOption,
		MatButton,
		RouterLink,
		MatIcon,
		LocalizeMapPipe,
		UserListComponent,
		ScopePickerComponent,
		AsyncPipe
	]
})
export class UserBrowseComponent implements OnInit {
	predicate = new UserSearch();
	profiles: Profile[];
	scopes: ScopeMini[];
	me?: User;
	roleStatus = RoleStatus;
	showDeleted = false;
	showExternallyManaged = false;

	roleStatusArray = Object.values(RoleStatus);
	getRoleStatusDisplay = getRoleStatusDisplay;

	searchForm = new FormGroup({
		fullText: new FormControl('', {nonNullable: true}),
		profileId: new FormControl('', {nonNullable: true}),
		status: new FormControl('', {nonNullable: true}),
		scopePk: new FormControl('', {nonNullable: true}),
		externallyManaged: new FormControl('', {nonNullable: true})
	});

	canWrite$: Observable<boolean>;

	constructor(
		private configurationService: ConfigurationService,
		private meService: MeService,
		private destroyRef: DestroyRef,
		private permissionsService: PermissionsService
	) {}

	ngOnInit() {
		this.canWrite$ = this.permissionsService.canWrite();
		forkJoin({
			profiles: this.configurationService.getProfiles(),
			scopes: this.meService.getScopes(undefined, true, false),
			me: this.meService.get()
		}).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(({profiles, scopes, me}) => {
			this.profiles = profiles;
			this.scopes = scopes;
			this.me = me;
			this.showExternallyManaged = this.me?.roles?.some(role => role.profileId === FeatureStatic.ADMIN) || false;
		});
	}

	search() {
		const {fullText, scopePk, profileId, status, externallyManaged} = this.searchForm.value;

		this.predicate = new UserSearch();
		this.predicate.fullText = fullText || undefined;
		this.predicate.scopePks = scopePk ? [Number(scopePk)] : [];
		this.predicate.profileIds = profileId ? [profileId] : [];
		this.predicate.states = status ? [status as RoleStatus] : [];
		this.predicate.externallyManaged = externallyManaged === '' ? undefined : Boolean(externallyManaged);
	}

	reset() {
		this.predicate = new UserSearch();
	}
}
