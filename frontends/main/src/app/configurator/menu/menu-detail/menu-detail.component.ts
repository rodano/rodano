import {Component, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {LanguageService} from '../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';
import {BaseManagerDetailComponent} from '../../shared/base-manager-detail.component';
import {SettingItemComponent} from '../../shared/setting-item/setting-item.component';
import {MenuConfig} from '@core/model/menu-config';
import {MenuManagerService} from '../../services/manager/menu-manager.service';
import {MenuDialogService} from '../../services/dialogs/menu-dialog.service';
import {ScopeModelManagerService} from '../../services/manager/scope-model-manager.service';

@Component({
	selector: 'app-menu-detail',
	standalone: true,
	templateUrl: './menu-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent, SettingItemComponent]
})
export class MenuDetailComponent extends BaseManagerDetailComponent<MenuConfig, MenuManagerService> {
	@Input() override entity!: MenuConfig;
	@Input() override allEntities: MenuConfig[] = [];
	@Output() menuUpdated = this.entityUpdated;
	@Output() menuDeleted = this.entityDeleted;

	@Input() set menu(v: MenuConfig) {this.entity = v;}
	get menu(): MenuConfig {return this.entity;}

	@Input() set allMenus(v: MenuConfig[]) {this.allEntities = v;}

	protected readonly Object = Object;

	constructor(
		private menuManager: MenuManagerService,
		languageService: LanguageService,
		private menuDialogService: MenuDialogService,
		private scopeModelManager: ScopeModelManagerService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(menuManager, languageService, snackBar);
	}

	protected getEntityId(): string {return this.entity.menuId;}

	get submenus(): MenuConfig[] {
		return this.allEntities
			.filter(m => m.parentMenuId === this.entity.menuId)
			.sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0));
	}

	onEditBasicInfo(): void {
		this.menuDialogService.openBasicInfoDialog(
			this.projectId,
			this.entity,
			this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onEditParent(): void {
		this.menuDialogService.openParentDialog(this.entity, this.allEntities)
			.subscribe(result => {
				if(result !== undefined) {
					this.applyUpdate({...this.entity, ...result});
				}
			});
	}

	onOrderSubmenus(): void {
		this.menuDialogService.openSubmenusOrderDialog(this.submenus)
			.subscribe((result: MenuConfig[]) => {
				if(result) {
					result.forEach(m => this.menuManager.update(m));
					this.entityUpdated.emit(this.entity);
				}
			});
	}

	onEditAction(): void {
		this.menuDialogService.openActionDialog(this.entity)
			.subscribe(result => {
				if(result) {
					this.applyUpdate({...this.entity, ...result});
				}
			});
	}

	onDelete(): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Menu',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.entity.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.entityDeleted.emit(this.entity.menuId);
			}
		});
	}

	objectEntries(obj: Record<string, string>): [string, string][] {
		return Object.entries(obj);
	}

	get isActionPageModified(): boolean {
		const original = this.menuManager.getOriginals().find(m => m.menuId === this.entity.menuId);
		return original?.action?.page !== this.entity.action?.page;
	}

	get isActionContextModified(): boolean {
		const original = this.menuManager.getOriginals().find(m => m.menuId === this.entity.menuId);
		const originalContext = original?.action?.context ?? [];
		const currentContext = this.entity.action?.context ?? [];
		return JSON.stringify(originalContext) !== JSON.stringify(currentContext);
	}

	get isActionParametersModified(): boolean {
		const original = this.menuManager.getOriginals().find(m => m.menuId === this.entity.menuId);
		const originalParams = original?.action?.parameters ?? {};
		const currentParams = this.entity.action?.parameters ?? {};
		return JSON.stringify(originalParams) !== JSON.stringify(currentParams);
	}

	get menuLevel(): 'standalone' | 'root' | 'intermediate' | 'leaf' {
		const hasParent = !!this.entity.parentMenuId;
		const hasChildren = this.allEntities.some(m => m.parentMenuId === this.entity.menuId);
		if(!hasParent && !hasChildren) {
			return 'standalone';
		}
		if(!hasParent && hasChildren) {
			return 'root';
		}
		if(hasParent && hasChildren) {
			return 'intermediate';
		}
		return 'leaf';
	}

	getContextLabel(contextItem: string): string {
		if(this.entity.action?.page === 'scopes') {
			return this.languageService.getLabelById(contextItem, id => this.scopeModelManager.getById(id));
		}
		return contextItem;
	}

	getMenuLabel(menuId: string): string {
		return this.languageService.getLabelById(menuId, id => this.menuManager.getById(id));
	}
}
