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
import {RuleDefinitionAction} from '@core/model/rule-definition-action';
import {RuleDefinitionActionManagerService} from '../../services/manager/rule-definition-action-manager.service';
import {
	RuleDefinitionActionDetailComponent
} from '../rule-definition-action-detail/rule-definition-action-detail.component';
import {RuleDefinitionActionDialogService} from '../../services/dialogs/rule-definition-action-dialog.service';

@Component({
	selector: 'app-rule-definition-action-list',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule,
		MatSnackBarModule, RuleDefinitionActionDetailComponent, EmptyStateComponent, ListHeaderComponent, ModifiedDirective],
	templateUrl: './rule-definition-action-list.component.html',
	styleUrls: ['../../shared/list-shared.css']
})
export class RuleDefinitionActionListComponent
	extends BaseListComponent<RuleDefinitionAction>
	implements OnInit, OnChanges, OnDestroy {
	@Input() override projectId = '';
	@Input() override project: ConfiguratorProject | null = null;
	@Input() override selectedNode: string | null = null;
	@Output() ruleDefinitionActionsChanged = new EventEmitter<boolean>();
	@Output() ruleDefinitionActionContextChanged = new EventEmitter<{
		ruleDefinitionActions: any[];
		selectedRuleDefinitionActionId: string | null;
	}>();

	constructor(
		public ruleDefinitionActionManager: RuleDefinitionActionManagerService,
		public override languageService: LanguageService,
		private ruleDefinitionActionDialogService: RuleDefinitionActionDialogService,
		snackBar: MatSnackBar
	) {
		super(ruleDefinitionActionManager, languageService, snackBar);
	}

	getEntityId(rda: RuleDefinitionAction): string {return rda.ruleDefinitionActionId;}
	getNodePrefix(): string {return 'ruleDefinitionAction';}
	getListNodeName(): string {return 'ruleDefinitionActions';}

	get ruleDefinitionActions(): RuleDefinitionAction[] {return this.ruleDefinitionActionManager.getAll();}
	get selectedRuleDefinitionAction(): RuleDefinitionAction | null {return this.selected as RuleDefinitionAction | null;}
	get modifiedRuleDefinitionActionIds(): Set<string> {return this.ruleDefinitionActionManager.getModifiedIds();}
	get originalRuleDefinitionActions(): RuleDefinitionAction[] {return this.ruleDefinitionActionManager.getOriginals();}

	loadRuleDefinitionActions(): void {this.load();}
	load(): void {
		this.loading = true;
		forkJoin({
			ruleDefinitionActions: this.ruleDefinitionActionManager.load(this.projectId)
		}).subscribe({
			next: ({ruleDefinitionActions}) => this.afterLoad(ruleDefinitionActions),
			error: (e: HttpErrorResponse) => this.handleLoadError(e, 'rule definition Actions')
		});
	}

	emitChangedEvent(hasModifications: boolean): void {
		this.ruleDefinitionActionsChanged.emit(hasModifications);
	}

	emitContextEvent(): void {
		this.ruleDefinitionActionContextChanged.emit({
			ruleDefinitionActions: [...this.ruleDefinitionActions],
			selectedRuleDefinitionActionId: this.selected?.ruleDefinitionActionId || null
		});
	}

	onCreate(): void {
		this.ruleDefinitionActionDialogService.openCreateDialog(this.projectId)
			.subscribe((result: RuleDefinitionAction | null) => {
				if(result) {
					this.ruleDefinitionActionManager.create(this.projectId, result).subscribe({
						next: () => this.afterCreate('Rule definition action'),
						error: e => {
							console.error(e);
							this.snackBar.open('Failed to create rule definition action', 'Close', {duration: 3000});
						}
					});
				}
			});
	}

	onUpdated(updated: RuleDefinitionAction): void {
		this.ruleDefinitionActionManager.update(updated);
		this.selected = this.ruleDefinitionActionManager.getById(updated.ruleDefinitionActionId) || null;
		this.emitModificationChange();
	}

	onDeleted(ruleDefinitionActionId: string): void {
		const ruleDefinitionAction = this.ruleDefinitionActions.find(r => r.ruleDefinitionActionId === ruleDefinitionActionId);
		if(!ruleDefinitionAction) {
			return;
		}
		this.ruleDefinitionActionManager.delete(this.projectId, ruleDefinitionAction.ruleDefinitionActionId).subscribe({
			next: () => this.afterDelete(ruleDefinitionAction, 'Rule definition Action'),
			error: (e: HttpErrorResponse) => {
				console.error(e);
				this.snackBar.open('Failed to delete rule definition Action', 'Close', {duration: 3000});
			}
		});
	}

	onSelectRuleDefinitionAction(rda: RuleDefinitionAction): void {this.onSelect(rda);}
	onCreateRuleDefinitionAction(): void {this.onCreate();}
	onRuleDefinitionActionUpdated(rda: RuleDefinitionAction): void {this.onUpdated(rda);}
	onRuleDefinitionActionDeleted(id: string): void {this.onDeleted(id);}
}
