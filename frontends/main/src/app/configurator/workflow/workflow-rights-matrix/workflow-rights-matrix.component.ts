import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Profile} from '@core/model/profile';
import {Workflow} from '@core/model/workflow';
import {WorkflowAction} from '@core/model/workflow-action';
import {LanguageService} from '../../services/language.service';
import {WorkflowRightsService} from '../../services/api/workflow-rights.service';

@Component({
	selector: 'app-workflow-rights-matrix',
	standalone: true,
	templateUrl: './workflow-rights-matrix.component.html',
	styleUrls: ['../../shared/matrix-shared.css', '../../shared/list-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule]
})
export class WorkflowRightsMatrixComponent implements OnInit {
	@Input() projectId = '';
	@Input() profiles: Profile[] = [];
	@Input() workflows: Workflow[] = [];
	@Input() workflowActions: WorkflowAction[] = [];
	@Output() closed = new EventEmitter<void>();

	workflowRights = new Map<string, Set<string>>();
	originalWorkflowRights = new Map<string, Set<string>>();

	actionRights = new Map<string, Set<string>>();
	originalActionRights = new Map<string, Set<string>>();

	loading = false;
	saving = false;
	modified = false;

	constructor(
		public languageService: LanguageService,
		private workflowRightsService: WorkflowRightsService,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.loadRights();
	}

	loadRights(): void {
		this.loading = true;
		this.workflowRightsService.getRights(this.projectId).subscribe({
			next: result => {
				this.workflowRights = new Map();
				this.profiles.forEach(p => {
					const workflowIds = (result.workflowRights ?? {})[p.profileId] ?? [];
					this.workflowRights.set(p.profileId, new Set(workflowIds));
				});

				this.actionRights = new Map();
				Object.entries(result.actionRights ?? {}).forEach(([actionId, profileIds]) => {
					this.actionRights.set(actionId, new Set(profileIds));
				});

				this.originalWorkflowRights = this.deepCopy(this.workflowRights);
				this.originalActionRights = this.deepCopy(this.actionRights);
				this.loading = false;
				this.modified = false;
			},
			error: () => {
				this.snackBar.open('Failed to load workflow rights', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	hasWorkflowRight(profileId: string, workflowId: string): boolean {
		return this.workflowRights.get(profileId)?.has(workflowId) ?? false;
	}

	toggleWorkflowRight(profileId: string, workflowId: string): void {
		const set = new Set(this.workflowRights.get(profileId) ?? []);
		if(set.has(workflowId)) {
			set.delete(workflowId);
		}
		else {
			set.add(workflowId);
		}
		this.workflowRights = new Map(this.workflowRights).set(profileId, set);
		this.modified = true;
	}

	getWorkflowRightCount(profileId: string): number {
		return this.workflowRights.get(profileId)?.size ?? 0;
	}

	isActionGranted(actionId: string, profileId: string): boolean {
		return this.actionRights.get(actionId)?.has(profileId) ?? false;
	}

	toggleActionRight(actionId: string, profileId: string): void {
		const set = new Set(this.actionRights.get(actionId) ?? []);
		if(set.has(profileId)) {
			set.delete(profileId);
		}
		else {
			set.add(profileId);
		}
		this.actionRights = new Map(this.actionRights).set(actionId, set);
		this.modified = true;
	}

	getActionsForWorkflow(workflowId: string): WorkflowAction[] {
		return this.workflowActions.filter(a => a.workflowId === workflowId);
	}

	onClear(): void {
		this.workflowRights = this.deepCopy(this.originalWorkflowRights);
		this.actionRights = this.deepCopy(this.originalActionRights);
		this.modified = false;
	}

	onSave(): void {
		this.saving = true;
		const workflowRightsRecord: Record<string, string[]> = {};
		this.workflowRights.forEach((ids, profileId) => {
			workflowRightsRecord[profileId] = Array.from(ids);
		});
		const actionRightsRecord: Record<string, string[]> = {};
		this.actionRights.forEach((profileIds, actionId) => {
			actionRightsRecord[actionId] = Array.from(profileIds);
		});

		this.workflowRightsService.saveRights(this.projectId, {
			workflowRights: workflowRightsRecord,
			actionRights: actionRightsRecord
		}).subscribe({
			next: () => {
				this.originalWorkflowRights = this.deepCopy(this.workflowRights);
				this.originalActionRights = this.deepCopy(this.actionRights);
				this.snackBar.open('Workflow rights saved', 'Close', {duration: 2000});
				this.saving = false;
				this.modified = false;
			},
			error: () => {
				this.snackBar.open('Failed to save workflow rights', 'Close', {duration: 3000});
				this.saving = false;
			}
		});
	}

	private deepCopy(source: Map<string, Set<string>>): Map<string, Set<string>> {
		const copy = new Map<string, Set<string>>();
		source.forEach((set, key) => copy.set(key, new Set(set)));
		return copy;
	}
}
