import {ChangeDetectionStrategy, Component, computed, DestroyRef, OnInit, signal} from '@angular/core';
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
import {forkJoin} from 'rxjs';
import {User} from '@core/model/user';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {RoleStatus} from '@core/model/role-status';
import {getRoleStatusDisplay} from '../role-status-display';
import {FeatureStatic} from '@core/model/feature-static';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
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
		ScopePickerComponent
	]
})
export class UserBrowseComponent implements OnInit {
	readonly predicate = signal<UserSearch>(new UserSearch());
	readonly profiles = signal<Profile[]>([]);
	readonly scopes = signal<ScopeMini[]>([]);
	readonly me = signal<User | undefined>(undefined);
	roleStatus = RoleStatus;
	showDeleted = false;
	readonly showExternallyManaged = computed(() => this.me()?.roles?.some(role => role.profileId === FeatureStatic.ADMIN) || false);

	roleStatusArray = Object.values(RoleStatus);
	getRoleStatusDisplay = getRoleStatusDisplay;

	searchForm = new FormGroup({
		fullText: new FormControl('', {nonNullable: true}),
		profileId: new FormControl('', {nonNullable: true}),
		status: new FormControl('', {nonNullable: true}),
		scopePk: new FormControl('', {nonNullable: true}),
		externallyManaged: new FormControl('', {nonNullable: true})
	});

	constructor(
		private configurationService: ConfigurationService,
		private meService: MeService,
		private destroyRef: DestroyRef
	) {}

	ngOnInit() {
		forkJoin({
			profiles: this.configurationService.getProfiles(),
			scopes: this.meService.getScopes(undefined, true, false),
			me: this.meService.get()
		}).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(({profiles, scopes, me}) => {
			this.profiles.set(profiles);
			this.scopes.set(scopes);
			this.me.set(me);
		});
	}

	search() {
		const {fullText, scopePk, profileId, status, externallyManaged} = this.searchForm.value;

		const predicate = new UserSearch();
		predicate.fullText = fullText || undefined;
		predicate.scopePks = scopePk ? [Number(scopePk)] : [];
		predicate.profileIds = profileId ? [profileId] : [];
		predicate.states = status ? [status as RoleStatus] : [];
		predicate.externallyManaged = externallyManaged === '' ? undefined : Boolean(externallyManaged);
		this.predicate.set(predicate);
	}

	reset() {
		this.predicate.set(new UserSearch());
	}
}
