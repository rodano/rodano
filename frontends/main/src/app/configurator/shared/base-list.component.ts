import {Directive, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {HttpErrorResponse} from '@angular/common/http';
import {Subscription} from 'rxjs';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {ProjectLanguage} from '@core/model/project-language';
import {LanguageService} from '../services/language.service';
import {BaseManagerService} from '../services/manager/base-manager.service';

@Directive()
export abstract class BaseListComponent<T extends Record<string, any>> implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Output() nodeSelected = new EventEmitter<string | null>();

	selected: T | null = null;
	viewMode = 'detail';
	loading = false;
	selectedLanguage = '';
	projectLanguages: ProjectLanguage[] = [];
	private languageSubscription!: Subscription;

	protected constructor(
		protected manager: BaseManagerService<T>,
		public languageService: LanguageService,
		protected snackBar: MatSnackBar
	) {}

	abstract getEntityId(entity: T): string;
	abstract getNodePrefix(): string;
	abstract getListNodeName(): string;
	abstract load(): void;
	abstract emitChangedEvent(hasModifications: boolean): void;
	abstract emitContextEvent(): void;
	abstract onCreate(): void;
	abstract onUpdated(entity: T): void;
	abstract onDeleted(id: string): void;

	ngOnInit(): void {
		this.projectLanguages = this.project?.languages?.length
			? this.project.languages
			: this.languageService.projectLanguages;
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(lang => {
			this.selectedLanguage = lang;
		});
		this.load();
	}

	ngOnChanges(changes: any): void {
		if(changes['selectedNode'] && this.manager.getAll().length > 0) {
			const prefix = `${this.getNodePrefix()}-`;
			if(this.selectedNode?.startsWith(prefix)) {
				const id = this.selectedNode.replace(prefix, '');
				const entity = this.manager.getAll().find(e => this.getEntityId(e) === id);
				if(entity) {
					this.selected = entity;
				}
			}
			else if(this.selectedNode === this.getListNodeName()) {
				this.selected = null;
			}
		}
	}

	ngOnDestroy(): void {this.languageSubscription?.unsubscribe();}

	get viewLevel(): number {return this.selected ? 2 : 0;}
	get modifiedIds(): Set<string> {return this.manager.getModifiedIds();}
	get originalEntities(): T[] {return this.manager.getOriginals();}
	get totalModificationCount(): number {return this.manager.getModificationCount();}

	onSelect(entity: T): void {
		if(this.selected && this.getEntityId(this.selected) === this.getEntityId(entity)) {
			this.clearSelection();
		}
		else {
			this.selectEntity(entity);
		}
		this.emitContextEvent();
	}

	clearSelection(): void {
		this.selected = null;
		this.viewMode = 'detail';
		this.nodeSelected.emit(this.getListNodeName());
	}

	selectById(id: string): void {
		const entity = this.manager.getAll().find(e => this.getEntityId(e) === id);
		if(entity) {
			this.selectEntity(entity);
			this.emitContextEvent();
		}
	}

	protected selectEntity(entity: T): void {
		const previousId = this.selected ? this.getEntityId(this.selected) : null;
		this.selected = entity;
		if(previousId !== this.getEntityId(entity)) {
			this.viewMode = 'detail';
		}
		this.nodeSelected.emit(`${this.getNodePrefix()}-${this.getEntityId(entity)}`);
	}

	isSelected(entity: T): boolean {
		return !!this.selected && this.getEntityId(this.selected) === this.getEntityId(entity);
	}

	isModified(id: string): boolean {return this.manager.isModified(id);}

	protected afterLoad(entities: T[]): void {
		if(this.selected) {
			this.selected = entities.find(e => this.getEntityId(e) === this.getEntityId(this.selected!)) || null;
		}
		this.loading = false;
		this.emitContextEvent();
	}

	protected handleLoadError(error: HttpErrorResponse, entityName: string): void {
		console.error(`Error loading ${entityName}:`, error);
		this.snackBar.open(`Failed to load ${entityName}`, 'Close', {duration: 3000});
		this.loading = false;
	}

	protected afterCreate(entityName: string): void {
		this.snackBar.open(`${entityName} created`, 'Close', {duration: 2000});
		this.load();
		this.emitModificationChange();
	}

	protected afterDelete(entity: T, entityName: string): void {
		this.snackBar.open(`${entityName} deleted`, 'Close', {duration: 2000});
		if(this.selected && this.getEntityId(this.selected) === this.getEntityId(entity)) {
			this.clearSelection();
		}
		this.load();
		this.emitModificationChange();
	}

	emitModificationChange(): void {this.emitChangedEvent(this.totalModificationCount > 0);}
}
