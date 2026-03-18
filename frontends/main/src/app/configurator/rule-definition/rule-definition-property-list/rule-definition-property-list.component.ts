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
import {RuleDefinitionProperty} from '@core/model/rule-definition-property';
import {RuleDefinitionPropertyManagerService} from '../../services/manager/rule-definition-property-manager.service';
import {
	RuleDefinitionPropertyDetailComponent
} from '../rule-definition-property-detail/rule-definition-property-detail.component';
import {RuleDefinitionPropertyDialogService} from '../../services/dialogs/rule-definition-property-dialog.service';

@Component({
	selector: 'app-rule-definition-property-list',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule,
		MatSnackBarModule, RuleDefinitionPropertyDetailComponent, EmptyStateComponent, ListHeaderComponent, ModifiedDirective],
	templateUrl: './rule-definition-property-list.component.html',
	styleUrls: ['../../shared/list-shared.css']
})
export class RuleDefinitionPropertyListComponent
	extends BaseListComponent<RuleDefinitionProperty>
	implements OnInit, OnChanges, OnDestroy {
	@Input() override projectId = '';
	@Input() override project: ConfiguratorProject | null = null;
	@Input() override selectedNode: string | null = null;
	@Output() ruleDefinitionPropertiesChanged = new EventEmitter<boolean>();
	@Output() ruleDefinitionPropertyContextChanged = new EventEmitter<{
		ruleDefinitionProperties: any[];
		selectedRuleDefinitionPropertyId: string | null;
	}>();

	constructor(
		public ruleDefinitionPropertyManager: RuleDefinitionPropertyManagerService,
		public override languageService: LanguageService,
		private ruleDefinitionPropertyDialogService: RuleDefinitionPropertyDialogService,
		snackBar: MatSnackBar
	) {
		super(ruleDefinitionPropertyManager, languageService, snackBar);
	}

	getEntityId(rdp: RuleDefinitionProperty): string {return rdp.ruleDefinitionPropertyId;}
	getNodePrefix(): string {return 'ruleDefinitionProperty';}
	getListNodeName(): string {return 'ruleDefinitionProperties';}

	get ruleDefinitionProperties(): RuleDefinitionProperty[] {return this.ruleDefinitionPropertyManager.getAll();}
	get selectedRuleDefinitionProperty(): RuleDefinitionProperty | null {return this.selected as RuleDefinitionProperty | null;}
	get modifiedRuleDefinitionPropertyIds(): Set<string> {return this.ruleDefinitionPropertyManager.getModifiedIds();}
	get originalRuleDefinitionProperties(): RuleDefinitionProperty[] {return this.ruleDefinitionPropertyManager.getOriginals();}

	loadRuleDefinitionProperties(): void {this.load();}
	load(): void {
		this.loading = true;
		forkJoin({
			ruleDefinitionProperties: this.ruleDefinitionPropertyManager.load(this.projectId)
		}).subscribe({
			next: ({ruleDefinitionProperties}) => this.afterLoad(ruleDefinitionProperties),
			error: (e: HttpErrorResponse) => this.handleLoadError(e, 'rule definition properties')
		});
	}

	emitChangedEvent(hasModifications: boolean): void {
		this.ruleDefinitionPropertiesChanged.emit(hasModifications);
	}

	emitContextEvent(): void {
		this.ruleDefinitionPropertyContextChanged.emit({
			ruleDefinitionProperties: [...this.ruleDefinitionProperties],
			selectedRuleDefinitionPropertyId: this.selected?.ruleDefinitionPropertyId || null
		});
	}

	onCreate(): void {
		this.ruleDefinitionPropertyDialogService.openCreateDialog(this.projectId)
			.subscribe((result: RuleDefinitionProperty | null) => {
				if(result) {
					this.ruleDefinitionPropertyManager.create(this.projectId, result).subscribe({
						next: () => this.afterCreate('Rule definition property'),
						error: e => {
							console.error(e);
							this.snackBar.open('Failed to create rule definition property', 'Close', {duration: 3000});
						}
					});
				}
			});
	}

	onUpdated(updated: RuleDefinitionProperty): void {
		this.ruleDefinitionPropertyManager.update(updated);
		this.selected = this.ruleDefinitionPropertyManager.getById(updated.ruleDefinitionPropertyId) || null;
		this.emitModificationChange();
	}

	onDeleted(ruleDefinitionPropertyId: string): void {
		const ruleDefinitionProperty = this.ruleDefinitionProperties.find(r => r.ruleDefinitionPropertyId === ruleDefinitionPropertyId);
		if(!ruleDefinitionProperty) {
			return;
		}
		this.ruleDefinitionPropertyManager.delete(this.projectId, ruleDefinitionProperty.ruleDefinitionPropertyId).subscribe({
			next: () => this.afterDelete(ruleDefinitionProperty, 'Rule definition property'),
			error: (e: HttpErrorResponse) => {
				console.error(e);
				this.snackBar.open('Failed to delete rule definition property', 'Close', {duration: 3000});
			}
		});
	}

	onSelectRuleDefinitionProperty(rdp: RuleDefinitionProperty): void {this.onSelect(rdp);}
	onCreateRuleDefinitionProperty(): void {this.onCreate();}
	onRuleDefinitionPropertyUpdated(rdp: RuleDefinitionProperty): void {this.onUpdated(rdp);}
	onRuleDefinitionPropertyDeleted(id: string): void {this.onDeleted(id);}
}
