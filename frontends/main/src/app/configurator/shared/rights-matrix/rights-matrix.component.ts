import {Directive, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Profile} from '@core/model/profile';
import {LanguageService} from '../../services/language.service';
import {Observable} from 'rxjs';
import {EntityRight} from '@core/model/entity-right';

export type RightsMap = Map<string, Map<string, EntityRight>>;

@Directive()
export abstract class BaseRightsMatrixComponent implements OnInit {
	@Input() projectId = '';
	@Input() profiles: Profile[] = [];
	@Output() closed = new EventEmitter<void>();

	rights: RightsMap = new Map();
	originalRights: RightsMap = new Map();
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
	abstract loadRightsFromApi(): Observable<Record<string, Record<string, EntityRight>>>;
	abstract saveRightsToApi(rights: Record<string, Record<string, EntityRight>>): Observable<void>;

	constructor(
		public languageService: LanguageService,
		protected snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.loadRights();
	}

	loadRights(): void {
		this.loading = true;
		this.loadRightsFromApi().subscribe({
			next: result => {
				this.rights = new Map();
				this.profiles.forEach(p => {
					const entityMap = new Map<string, EntityRight>();
					const profileRights = result[p.profileId] ?? {};
					Object.entries(profileRights).forEach(([entityId, right]) => {
						entityMap.set(entityId, right);
					});
					this.rights.set(p.profileId, entityMap);
				});
				this.originalRights = this.deepCopy(this.rights);
				this.loading = false;
				this.modified = false;
			},
			error: () => {
				this.snackBar.open('Failed to load rights', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	getRight(profileId: string, entityId: string): EntityRight {
		return this.rights.get(profileId)?.get(entityId) ?? {canRead: false, canWrite: false};
	}

	toggleRead(profileId: string, entityId: string): void {
		const current = this.getRight(profileId, entityId);
		let next: EntityRight;
		if(current.canWrite) {
			next = {canRead: !current.canRead, canWrite: false};
		}
		else {
			next = {canRead: !current.canRead, canWrite: false};
		}
		this.setRight(profileId, entityId, next);
	}

	toggleWrite(profileId: string, entityId: string): void {
		const current = this.getRight(profileId, entityId);
		if(current.canWrite) {
			this.setRight(profileId, entityId, {canRead: current.canRead, canWrite: false});
		}
		else {
			this.setRight(profileId, entityId, {canRead: true, canWrite: true});
		}
	}

	private setRight(profileId: string, entityId: string, right: EntityRight): void {
		const profileMap = new Map(this.rights.get(profileId) ?? new Map());
		if(!right.canRead && !right.canWrite) {
			profileMap.delete(entityId);
		}
		else {
			profileMap.set(entityId, right);
		}
		this.rights = new Map(this.rights).set(profileId, profileMap);
		this.modified = true;
	}

	getGrantCount(profileId: string): number {
		return this.rights.get(profileId)?.size ?? 0;
	}

	isAllWriteForProfile(profileId: string): boolean {
		const map = this.rights.get(profileId) ?? new Map();
		return this.entities.every(e => map.get(this.getEntityId(e))?.canWrite);
	}

	toggleAllWriteForProfile(profileId: string): void {
		const all = this.isAllWriteForProfile(profileId);
		const profileMap = new Map<string, EntityRight>();
		if(!all) {
			this.entities.forEach(e => profileMap.set(this.getEntityId(e), {canRead: true, canWrite: true}));
		}
		this.rights = new Map(this.rights).set(profileId, profileMap);
		this.modified = true;
	}

	isAllWriteForEntity(entityId: string): boolean {
		return this.profiles.every(p => this.rights.get(p.profileId)?.get(entityId)?.canWrite);
	}

	toggleAllWriteForEntity(entityId: string): void {
		const all = this.isAllWriteForEntity(entityId);
		const newRights = new Map(this.rights);
		this.profiles.forEach(p => {
			const profileMap = new Map(newRights.get(p.profileId) ?? new Map());
			if(all) {
				profileMap.delete(entityId);
			}
			else {
				profileMap.set(entityId, {canRead: true, canWrite: true});
			}
			newRights.set(p.profileId, profileMap);
		});
		this.rights = newRights;
		this.modified = true;
	}

	onClear(): void {
		this.rights = this.deepCopy(this.originalRights);
		this.modified = false;
	}

	onSave(): void {
		this.saving = true;
		const result: Record<string, Record<string, EntityRight>> = {};
		this.rights.forEach((entityMap, profileId) => {
			result[profileId] = {};
			entityMap.forEach((right, entityId) => {
				result[profileId][entityId] = right;
			});
		});
		this.saveRightsToApi(result).subscribe({
			next: () => {
				this.originalRights = this.deepCopy(this.rights);
				this.snackBar.open('Rights saved', 'Close', {duration: 2000});
				this.saving = false;
				this.modified = false;
			},
			error: () => {
				this.snackBar.open('Failed to save rights', 'Close', {duration: 3000});
				this.saving = false;
			}
		});
	}

	private deepCopy(source: RightsMap): RightsMap {
		const copy: RightsMap = new Map();
		source.forEach((entityMap, profileId) => {
			copy.set(profileId, new Map(entityMap));
		});
		return copy;
	}
}
