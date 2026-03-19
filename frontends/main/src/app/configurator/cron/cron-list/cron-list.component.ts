import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {forkJoin} from 'rxjs';
import {LanguageService} from '../../services/language.service';
import {HttpErrorResponse} from '@angular/common/http';
import {EmptyStateComponent} from '../../shared/empty-state/empty-state.component';
import {BaseListComponent} from '../../shared/base-list.component';
import {ListHeaderComponent} from '../../shared/list-header/list-header.component';
import {ModifiedDirective} from '../../shared/modified.directive';
import {Cron} from '@core/model/cron';
import {CronManagerService} from '../../services/manager/cron-manager.service';
import {CronDetailComponent} from '../cron-detail/cron-detail.component';
import {CronDialogService} from '../../services/dialogs/cron-dialog.service';

@Component({
	selector: 'app-cron-list',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule,
		MatSnackBarModule, CronDetailComponent, EmptyStateComponent, ListHeaderComponent, ModifiedDirective],
	templateUrl: './cron-list.component.html',
	styleUrls: ['../../shared/list-shared.css']
})
export class CronListComponent
	extends BaseListComponent<Cron>
	implements OnInit, OnChanges, OnDestroy {
	@Input() override projectId = '';
	@Input() override project: ConfiguratorProject | null = null;
	@Input() override selectedNode: string | null = null;
	@Output() cronsChanged = new EventEmitter<boolean>();
	@Output() cronContextChanged = new EventEmitter<{
		crons: any[];
		selectedCronId: string | null;
	}>();

	constructor(
		public cronManager: CronManagerService,
		public override languageService: LanguageService,
		private cronDialogService: CronDialogService,
		snackBar: MatSnackBar
	) {
		super(cronManager, languageService, snackBar);
	}

	getEntityId(f: Cron): string {return f.cronId;}
	getNodePrefix(): string {return 'cron';}
	getListNodeName(): string {return 'crons';}

	get crons(): Cron[] {return this.cronManager.getAll();}
	get selectedCron(): Cron | null {return this.selected as Cron | null;}
	get modifiedCronIds(): Set<string> {return this.cronManager.getModifiedIds();}
	get originalCrons(): Cron[] {return this.cronManager.getOriginals();}

	loadCrons(): void {this.load();}
	load(): void {
		this.loading = true;
		forkJoin({
			crons: this.cronManager.load(this.projectId)
		}).subscribe({
			next: ({crons}) => this.afterLoad(crons),
			error: (e: HttpErrorResponse) => this.handleLoadError(e, 'crons')
		});
	}

	emitChangedEvent(hasModifications: boolean): void {
		this.cronsChanged.emit(hasModifications);
	}

	emitContextEvent(): void {
		this.cronContextChanged.emit({
			crons: [...this.crons],
			selectedCronId: this.selected?.cronId || null
		});
	}

	onCreate(): void {
		this.cronDialogService.openCreateDialog(this.projectId, this.projectLanguages)
			.subscribe((result: Cron | null) => {
				if(result) {
					this.cronManager.create(this.projectId, result).subscribe({
						next: () => this.afterCreate('cron'),
						error: e => {
							console.error(e);
							this.snackBar.open('Failed to create cron', 'Close', {duration: 3000});
						}
					});
				}
			});
	}

	onUpdated(updated: Cron): void {
		this.cronManager.update(updated);
		this.selected = this.cronManager.getById(updated.cronId) || null;
		this.emitModificationChange();
	}

	onDeleted(cronId: string): void {
		const cron = this.crons.find(f => f.cronId === cronId);
		if(!cron) {
			return;
		}
		this.cronManager.delete(this.projectId, cronId).subscribe({
			next: () => this.afterDelete(cron, 'cron'),
			error: (e: HttpErrorResponse) => {
				console.error(e);
				this.snackBar.open('Failed to delete cron', 'Close', {duration: 3000});
			}
		});
	}

	onSelectCron(c: Cron): void {this.onSelect(c);}
	onCreateCron(): void {this.onCreate();}
	onCronUpdated(c: Cron): void {this.onUpdated(c);}
	onCronDeleted(id: string): void {this.onDeleted(id);}
}
