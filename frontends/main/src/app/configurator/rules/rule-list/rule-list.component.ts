import {Component, Input, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatSnackBar} from '@angular/material/snack-bar';
import {RuleService} from '../../services/api/rule.service';
import {LanguageService} from '../../services/language.service';
import {Rule} from '@core/model/rule';

@Component({
	selector: 'app-rule-list',
	standalone: true,
	templateUrl: './rule-list.component.html',
	styleUrls: ['./rule-list.component.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule]
})
export class RuleListComponent implements OnInit {
	@Input() projectId = '';
	@Input() entityPath = '';
	@Input() ruleTypes: {type: string | null; label: string}[] = [];

	rules: Rule[] = [];
	loading = false;
	selectedRule: Rule | null = null;

	constructor(
		public languageService: LanguageService,
		private ruleService: RuleService,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.loadRules();
	}

	loadRules(): void {
		this.loading = true;
		this.ruleService.getRules(this.projectId, this.entityPath).subscribe({
			next: rules => {
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
		return this.rules.filter(r => (r as any).ruleType === type);
	}

	onSelectRule(rule: Rule): void {
		this.selectedRule = this.selectedRule?.ruleId === rule.ruleId ? null : rule;
	}

	onAddRule(type: string | null): void {
		const newRule: Rule = {
			description: '',
			message: {},
			tags: [],
			constraint: {
				conditions: {
					SCOPE: {mode: 'OR', conditions: []},
					EVENT: {mode: 'OR', conditions: []},
					DATASET: {mode: 'OR', conditions: []},
					FIELD: {mode: 'OR', conditions: []},
					FORM: {mode: 'OR', conditions: []},
					WORKFLOW: {mode: 'OR', conditions: []}
				}
			},
			actions: []
		};

		this.ruleService.createRule(this.projectId, this.entityPath, newRule).subscribe({
			next: created => {
				this.rules.push(created);
				this.selectedRule = created;
				this.snackBar.open('Rule created', 'Close', {duration: 2000});
			},
			error: () => this.snackBar.open('Failed to create rule', 'Close', {duration: 3000})
		});
	}

	onDeleteRule(rule: Rule): void {
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
	}

	onRuleSaved(saved: Rule): void {
		const idx = this.rules.findIndex(r => r.ruleId === saved.ruleId);
		if(idx !== -1) {
			this.rules[idx] = saved;
			this.selectedRule = saved;
		}
	}
}
