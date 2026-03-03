import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {MatTooltip} from '@angular/material/tooltip';
import {HttpErrorResponse} from '@angular/common/http';
import {forkJoin, of} from 'rxjs';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {Validator} from '@core/model/validator';
import {ValidatorManagerService} from '../../services/manager/validator-manager.service';
import {ValidatorDialogService} from '../../services/dialogs/validator-dialog.service';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {WorkflowStateManagerService} from '../../services/manager/workflow-state-manager.service';
import {LanguageService} from '../../services/language.service';
import {ValidatorDetailComponent} from '../validator-detail/validator-detail.component';
import {EmptyStateComponent} from '../../shared/empty-state/empty-state.component';
import {BaseListComponent} from '../../shared/base-list.component';
import {ListHeaderComponent} from '../../shared/list-header/list-header.component';

@Component({
	selector: 'app-validators-list',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule,
		MatSnackBarModule, MatTooltip, ValidatorDetailComponent, EmptyStateComponent, ListHeaderComponent],
	templateUrl: './validators-list.component.html',
	styleUrls: ['../../shared/list-shared.css']
})
export class ValidatorsListComponent
	extends BaseListComponent<Validator>
	implements OnInit, OnChanges, OnDestroy {
	@Input() override projectId = '';
	@Input() override project: ConfiguratorProject | null = null;
	@Input() override selectedNode: string | null = null;
	@Output() validatorsChanged = new EventEmitter<{modificationCount: number}>();
	@Output() validatorContextChanged = new EventEmitter<{
		validators: any[];
		selectedValidatorId: string | null;
	}>();

	constructor(
		public validatorManager: ValidatorManagerService,
		public override languageService: LanguageService,
		private workflowManager: WorkflowManagerService,
		private workflowStateManager: WorkflowStateManagerService,
		private validatorDialogService: ValidatorDialogService,
		snackBar: MatSnackBar
	) {
		super(validatorManager, languageService, snackBar);
	}

	getEntityId(v: Validator): string {return v.validatorId;}
	getNodePrefix(): string {return 'validator';}
	getListNodeName(): string {return 'validators';}

	get validators(): Validator[] {return this.manager.getAll();}
	get selectedValidator(): Validator | null {return this.selected as Validator | null;}
	get modifiedValidatorIds(): Set<string> {return this.manager.getModifiedIds();}
	get originalValidators(): Validator[] {return this.manager.getOriginals();}

	loadValidators(): void {this.load();}
	load(): void {
		this.loading = true;
		forkJoin({
			validators: this.validatorManager.load(this.projectId),
			workflowStates: this.workflowStateManager.isLoaded()
				? of(null)
				: this.workflowStateManager.load(this.projectId)
		}).subscribe({
			next: ({validators}) => this.afterLoad(validators),
			error: (e: HttpErrorResponse) => this.handleLoadError(e, 'validators')
		});
	}

	emitChangedEvent(count: number): void {
		this.validatorsChanged.emit({modificationCount: count});
	}

	emitContextEvent(): void {
		this.validatorContextChanged.emit({
			validators: [...this.validators],
			selectedValidatorId: this.selected?.validatorId || null
		});
	}

	onCreate(): void {
		this.validatorDialogService.openCreateDialog(this.projectId, this.projectLanguages)
			.subscribe((result: Validator | null) => {
				if(result) {
					this.validatorManager.create(this.projectId, result).subscribe({
						next: () => this.afterCreate('Validator'),
						error: e => {
							console.error(e);
							this.snackBar.open('Failed to create validator', 'Close', {duration: 3000});
						}
					});
				}
			});
	}

	onUpdated(updated: Validator): void {
		this.selected = this.validatorManager.getById(updated.validatorId) || null;
		this.emitModificationChange();
	}

	onDeleted(validatorId: string): void {
		const validator = this.validators.find(v => v.validatorId === validatorId);
		if(!validator) {
			return;
		}
		this.validatorManager.delete(this.projectId, validatorId).subscribe({
			next: () => this.afterDelete(validator, 'Validator'),
			error: (e: HttpErrorResponse) => {
				console.error(e);
				this.snackBar.open('Failed to delete validator', 'Close', {duration: 3000});
			}
		});
	}

	onSelectValidator(v: Validator): void {this.onSelect(v);}
	onCreateValidator(): void {this.onCreate();}
	onValidatorUpdated(v: Validator): void {this.onUpdated(v);}
	onValidatorDeleted(id: string): void {this.onDeleted(id);}

	getWorkflowLabel(id: string): string {
		return this.languageService.getLabelById(id, i => this.workflowManager.getById(i));
	}

	getWorkflowStateLabel(id: string): string {
		return this.languageService.getLabelById(id, i => this.workflowStateManager.getById(i));
	}
}
