import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {forkJoin, of} from 'rxjs';
import {LanguageService} from '../../services/language.service';
import {HttpErrorResponse} from '@angular/common/http';
import {EmptyStateComponent} from '../../shared/empty-state/empty-state.component';
import {BaseListComponent} from '../../shared/base-list.component';
import {ListHeaderComponent} from '../../shared/list-header/list-header.component';
import {ModifiedDirective} from '../../shared/modified.directive';
import {MenuConfig} from '@core/model/menu-config';
import {MenuManagerService} from '../../services/manager/menu-manager.service';
import {MatTooltip} from '@angular/material/tooltip';
import {MenuDetailComponent} from '../menu-detail/menu-detail.component';
import {MenuDialogService} from '../../services/dialogs/menu-dialog.service';
import {ProfileManagerService} from '../../services/manager/profile-manager.service';
import {Profile} from '@core/model/profile';
import {MenuGrantsMatrixComponent} from '../menu-grants-matrix/menu-grants-matrix.component';
import {EventModelManagerService} from '../../services/manager/event-model-manager.service';

@Component({
	selector: 'app-menu-list',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule, MatSnackBarModule,
		MenuDetailComponent, EmptyStateComponent, ListHeaderComponent, ModifiedDirective, MatTooltip, MenuGrantsMatrixComponent],
	templateUrl: './menu-list.component.html',
	styleUrls: ['../../shared/list-shared.css']
})
export class MenuListComponent
	extends BaseListComponent<MenuConfig>
	implements OnInit, OnChanges, OnDestroy {
	@Input() override projectId = '';
	@Input() override project: ConfiguratorProject | null = null;
	@Input() override selectedNode: string | null = null;
	@Output() menusChanged = new EventEmitter<boolean>();
	@Output() menuContextChanged = new EventEmitter<{
		menus: any[];
		selectedMenuId: string | null;
	}>();

	showMatrix = false;

	constructor(
		public menuManager: MenuManagerService,
		public override languageService: LanguageService,
		private profileManager: ProfileManagerService,
		private eventModelManager: EventModelManagerService,
		private menuDialogService: MenuDialogService,
		snackBar: MatSnackBar
	) {
		super(menuManager, languageService, snackBar);
	}

	getEntityId(m: MenuConfig): string {return m.menuId;}
	getNodePrefix(): string {return 'menu';}
	getListNodeName(): string {return 'menus';}

	get menus(): MenuConfig[] {return this.menuManager.getAll();}
	get selectedMenu(): MenuConfig | null {return this.selected as MenuConfig | null;}
	get modifiedMenuIds(): Set<string> {return this.menuManager.getModifiedIds();}
	get originalMenus(): MenuConfig[] {return this.menuManager.getOriginals();}

	get profiles(): Profile[] {return this.profileManager.getAll();}

	loadMenus(): void {this.load();}
	load(): void {
		this.loading = true;
		forkJoin({
			menus: this.menuManager.load(this.projectId),
			eventModels: this.eventModelManager.isLoaded()
				? of(null)
				: this.eventModelManager.load(this.projectId)
		}).subscribe({
			next: ({menus}) => this.afterLoad(menus),
			error: (e: HttpErrorResponse) => this.handleLoadError(e, 'menus')
		});
	}

	emitChangedEvent(hasModifications: boolean): void {
		this.menusChanged.emit(hasModifications);
	}

	emitContextEvent(): void {
		this.menuContextChanged.emit({
			menus: [...this.menus],
			selectedMenuId: this.selected?.menuId || null
		});
	}

	onCreate(): void {
		this.menuDialogService.openCreateDialog(this.projectId, this.projectLanguages)
			.subscribe((result: MenuConfig | null) => {
				if(result) {
					this.menuManager.create(this.projectId, result).subscribe({
						next: () => this.afterCreate('menu'),
						error: e => {
							console.error(e);
							this.snackBar.open('Failed to create menu', 'Close', {duration: 3000});
						}
					});
				}
			});
	}

	onUpdated(updated: MenuConfig): void {
		this.menuManager.update(updated);
		this.selected = this.menuManager.getById(updated.menuId) || null;
		this.emitModificationChange();
	}

	onDeleted(menuId: string): void {
		const menu = this.menus.find(m => m.menuId === menuId);
		if(!menu) {
			return;
		}
		this.menuManager.delete(this.projectId, menu.menuId).subscribe({
			next: () => this.afterDelete(menu, 'menu'),
			error: (e: HttpErrorResponse) => {
				console.error(e);
				this.snackBar.open('Failed to delete menu', 'Close', {duration: 3000});
			}
		});
	}

	onSelectMenu(m: MenuConfig): void {this.onSelect(m);}
	onCreateMenu(): void {this.onCreate();}
	onMenuUpdated(m: MenuConfig): void {this.onUpdated(m);}
	onMenuDeleted(id: string): void {this.onDeleted(id);}

	getMenuLevel(menu: MenuConfig): 'standalone' | 'root' | 'intermediate' | 'leaf' {
		const hasParent = !!menu.parentMenuId;
		const hasChildren = this.menuManager.getAll().some(m => m.parentMenuId === menu.menuId);
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

	onToggleMatrix(): void {
		this.showMatrix = !this.showMatrix;
		if(this.showMatrix) {
			this.selected = null;
		}
	}
}
