import {Directive, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Profile} from '@core/model/profile';
import {LanguageService} from '../../services/language.service';
import {Observable} from 'rxjs';

@Directive()
export abstract class BaseGrantsMatrixComponent implements OnInit {
	@Input() projectId = '';
	@Input() profiles: Profile[] = [];
	@Output() closed = new EventEmitter<void>();

	grants = new Map<string, Set<string>>();
	originalGrants = new Map<string, Set<string>>();
	loading = false;
	saving = false;
	modified = false;

	abstract get title(): string;
	abstract get subtitle(): string;
	abstract get themeClass(): string;
	abstract get entities(): any[];
	abstract getEntityId(entity: any): string;
	abstract getEntityLabel(entity: any): string;
	abstract getEntityCode(entity: any): string;
	abstract getEntityCount(): number;
	abstract loadGrantsFromApi(): Observable<Record<string, string[]>>;
	abstract saveGrantsToApi(grants: Record<string, string[]>): Observable<void>;

	constructor(
		public languageService: LanguageService,
		protected snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.loadGrants();
	}

	loadGrants(): void {
		this.loading = true;
		this.loadGrantsFromApi().subscribe({
			next: result => {
				this.grants = new Map();
				this.profiles.forEach(p => {
					const ids = result[p.profileId] ?? [];
					this.grants.set(p.profileId, new Set(ids));
				});
				this.originalGrants = this.deepCopyGrants(this.grants);
				this.loading = false;
				this.modified = false;
			},
			error: () => {
				this.snackBar.open('Failed to load grants', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	isGranted(profileId: string, entityId: string): boolean {
		return this.grants.get(profileId)?.has(entityId) ?? false;
	}

	toggleGrant(profileId: string, entityId: string): void {
		const set = new Set(this.grants.get(profileId) ?? []);
		if(set.has(entityId)) {
			set.delete(entityId);
		}
		else {
			set.add(entityId);
		}
		this.grants = new Map(this.grants).set(profileId, set);
		this.modified = true;
	}

	isAllGrantedForProfile(profileId: string): boolean {
		const set = this.grants.get(profileId) ?? new Set();
		return this.entities.every(e => set.has(this.getEntityId(e)));
	}

	toggleAllForProfile(profileId: string): void {
		const newSet = this.isAllGrantedForProfile(profileId)
			? new Set<string>()
			: new Set(this.entities.map(e => this.getEntityId(e)));
		this.grants = new Map(this.grants).set(profileId, newSet);
		this.modified = true;
	}

	isAllGrantedForEntity(entityId: string): boolean {
		return this.profiles.every(p => this.grants.get(p.profileId)?.has(entityId));
	}

	toggleAllForEntity(entityId: string): void {
		const newGrants = new Map(this.grants);
		if(this.isAllGrantedForEntity(entityId)) {
			this.profiles.forEach(p => {
				const set = new Set(newGrants.get(p.profileId) ?? []);
				set.delete(entityId);
				newGrants.set(p.profileId, set);
			});
		}
		else {
			this.profiles.forEach(p => {
				const set = new Set(newGrants.get(p.profileId) ?? []);
				set.add(entityId);
				newGrants.set(p.profileId, set);
			});
		}
		this.grants = newGrants;
		this.modified = true;
	}

	getGrantCount(profileId: string): number {
		return this.grants.get(profileId)?.size ?? 0;
	}

	onClear(): void {
		this.grants = this.deepCopyGrants(this.originalGrants);
		this.modified = false;
	}

	onSave(): void {
		this.saving = true;
		const result: Record<string, string[]> = {};
		this.grants.forEach((ids, profileId) => {
			result[profileId] = Array.from(ids);
		});
		this.saveGrantsToApi(result).subscribe({
			next: () => {
				this.originalGrants = this.deepCopyGrants(this.grants);
				this.snackBar.open('Grants saved', 'Close', {duration: 2000});
				this.saving = false;
				this.modified = false;
			},
			error: () => {
				this.snackBar.open('Failed to save grants', 'Close', {duration: 3000});
				this.saving = false;
			}
		});
	}

	private deepCopyGrants(source: Map<string, Set<string>>): Map<string, Set<string>> {
		const copy = new Map<string, Set<string>>();
		source.forEach((set, key) => copy.set(key, new Set(set)));
		return copy;
	}
}
