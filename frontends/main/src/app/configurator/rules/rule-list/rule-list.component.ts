import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatSnackBar} from '@angular/material/snack-bar';
import {RuleService} from '../../services/api/rule.service';
import {LanguageService} from '../../services/language.service';
import {Rule} from '@core/model/rule';
import {forkJoin, of} from 'rxjs';
import {FieldModelManagerService} from '../../services/manager/field-model-manager.service';
import {EventModelManagerService} from '../../services/manager/event-model-manager.service';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {EventGroupManagerService} from '../../services/manager/event-group-manager.service';
import {WorkflowActionManagerService} from '../../services/manager/workflow-action-manager.service';
import {WorkflowStateManagerService} from '../../services/manager/workflow-state-manager.service';

@Component({
	selector: 'app-rule-list',
	standalone: true,
	templateUrl: './rule-list.component.html',
	styleUrls: ['./rule-list.component.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule]
})
export class RuleListComponent implements OnInit, OnChanges {
	@Input() projectId = '';
	@Input() entityPath = '';
	@Input() ruleTypes: {type: string | null; label: string}[] = [];
	@Input() availableDomains: string[] = ['SCOPE', 'EVENT', 'DATASET', 'FIELD', 'FORM', 'WORKFLOW'];
	@Output() editRule = new EventEmitter<Rule>();

	rules: Rule[] = [];
	loading = false;
	selectedRule: Rule | null = null;

	constructor(
		public languageService: LanguageService,
		private ruleService: RuleService,
		private fieldModelManager: FieldModelManagerService,
		private eventModelManager: EventModelManagerService,
		private eventGroupManager: EventGroupManagerService,
		private workflowActionManager: WorkflowActionManagerService,
		private workflowStateManager: WorkflowStateManagerService,
		private snackBar: MatSnackBar,
		private dialog: MatDialog
	) {}

	ngOnInit(): void {
		this.loadRules();
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['entityPath'] && !changes['entityPath'].firstChange) {
			this.loadRules();
		}
	}

	loadRules(): void {
		this.loading = true;
		forkJoin({
			rules: this.ruleService.getRules(this.projectId, this.entityPath),
			fieldModels: this.fieldModelManager.isLoaded()
				? of(null)
				: this.fieldModelManager.loadFull(this.projectId),
			eventModels: this.eventModelManager.isLoaded()
				? of(null)
				: this.eventModelManager.load(this.projectId),
			eventGroups: this.eventGroupManager.isLoaded()
				? of(null)
				: this.eventGroupManager.load(this.projectId),
			workflowActions: this.workflowActionManager.isLoaded()
				? of(null)
				: this.workflowActionManager.load(this.projectId),
			workflowStates: this.workflowStateManager.isLoaded()
				? of(null)
				: this.workflowStateManager.load(this.projectId)
		}).subscribe({
			next: ({rules}) => {
				this.rules = rules;
				this.loading = false;
			},
			error: () => {
				this.snackBar.open('Failed to load rules', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	getRulesForType(type: string | null): Rule[] {
		if(type === null) {
			return this.rules;
		}
		return this.rules.filter(r => r.ruleType === type);
	}

	onSelectRule(rule: Rule): void {
		this.editRule.emit(rule);
	}

	onAddRule(type: string | null): void {
		const newRule: Rule = {
			ruleType: type ?? undefined,
			description: '',
			message: {},
			tags: [],
			actions: []
		};

		this.ruleService.createRule(this.projectId, this.entityPath, newRule).subscribe({
			next: created => {
				this.rules = [...this.rules, created];
				this.selectedRule = created;
				this.snackBar.open('Rule created', 'Close', {duration: 2000});
			},
			error: () => this.snackBar.open('Failed to create rule', 'Close', {duration: 3000})
		});
	}

	onDeleteRule(rule: Rule): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Rule',
				message: `Are you sure you want to delete "${rule.description || 'Unnamed rule'}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe((confirmed: boolean) => {
			if(!confirmed) {
				return;
			}
			this.ruleService.deleteRule(this.projectId, this.entityPath, rule.ruleId!).subscribe({
				next: () => {
					this.rules = this.rules.filter(r => r.ruleId !== rule.ruleId);
					if(this.selectedRule?.ruleId === rule.ruleId) {
						this.selectedRule = null;
					}
					this.snackBar.open('Rule deleted', 'Close', {duration: 2000});
				},
				error: () => this.snackBar.open('Failed to delete rule', 'Close', {duration: 3000})
			});
		});
	}

	onRuleSaved(saved: Rule): void {
		const idx = this.rules.findIndex(r => r.ruleId === saved.ruleId);
		if(idx !== -1) {
			this.rules[idx] = saved;
			this.selectedRule = saved;
		}
	}
}
