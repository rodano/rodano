import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, ViewChild} from '@angular/core';
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
import {Rule} from '@core/model/rule';
import {RuleDetailComponent} from '../../rules/rule-detail/rule-detail.component';
import {MatTooltip} from '@angular/material/tooltip';

type CronViewMode = 'list' | 'detail' | 'rule-editor';

@Component({
	selector: 'app-cron-list',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule, MatSnackBarModule,
		CronDetailComponent, EmptyStateComponent, ListHeaderComponent, ModifiedDirective, RuleDetailComponent, MatTooltip],
	templateUrl: './cron-list.component.html',
	styleUrls: ['../../shared/list-shared.css', '../../shared/breadcrumb-shared.css']
})
export class CronListComponent
	extends BaseListComponent<Cron>
	implements OnInit, OnChanges, OnDestroy {
	@ViewChild(RuleDetailComponent) ruleDetailComponent!: RuleDetailComponent;

	@Input() override projectId = '';
	@Input() override project: ConfiguratorProject | null = null;
	@Input() override selectedNode: string | null = null;
	@Output() cronsChanged = new EventEmitter<boolean>();
	@Output() cronContextChanged = new EventEmitter<{
		crons: any[];
		selectedCronId: string | null;
	}>();

	cronViewMode: CronViewMode = 'list';
	selectedRule: Rule | null = null;
	ruleModified = false;
	returnTab: 'general' | 'rules' = 'general';

	readonly ruleDomains = ['SCOPE'];

	private originalRule: Rule | null = null;

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

	onSelectCron(c: Cron): void {
		this.onSelect(c);
		this.cronViewMode = 'detail';
	}

	onCreateCron(): void {this.onCreate();}
	onCronUpdated(c: Cron): void {this.onUpdated(c);}
	onCronDeleted(id: string): void {this.onDeleted(id);}

	switchToRuleEditor(rule: Rule): void {
		this.selectedRule = rule;
		this.originalRule = JSON.parse(JSON.stringify(rule));
		this.ruleModified = false;
		this.cronViewMode = 'rule-editor';
	}

	onRuleChanged(): void {
		this.ruleModified = true;
	}

	backToCronDetail(tab: 'general' | 'rules' = 'general'): void {
		this.selectedRule = null;
		this.returnTab = tab;
		this.cronViewMode = 'detail';
	}

	onSaveRule(): void {
		this.ruleDetailComponent?.onSave();
	}

	onRevertRule(): void {
		if(this.originalRule) {
			this.selectedRule = JSON.parse(JSON.stringify(this.originalRule));
			this.ruleModified = false;
		}
	}

	onRuleSaved(rule: Rule): void {
		this.selectedRule = rule;
		this.originalRule = JSON.parse(JSON.stringify(rule));
		this.ruleModified = false;
	}
}
