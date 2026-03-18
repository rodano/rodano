import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatSelectModule} from '@angular/material/select';
import {FormsModule} from '@angular/forms';
import {BaseDialogComponent} from '../../base-dialog.component';
import {RuleDefinitionActionParameter} from '@core/model/rule-definition-action-parameter';
import {ENTITY_OPTIONS} from '../entity-options';
import {CONFIGURATION_ENTITY_OPTIONS} from '../configuration-entity-options';

export interface RuleDefinitionActionParameterDialogData {
	parameters: RuleDefinitionActionParameter[];
}

@Component({
	selector: 'app-rule-definition-action-parameter-dialog',
	standalone: true,
	templateUrl: './rule-definition-action-parameter-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule, MatSelectModule, FormsModule]
})
export class RuleDefinitionActionParameterDialogComponent
	extends BaseDialogComponent<RuleDefinitionActionParameterDialogData>
	implements OnInit {
	parameters: RuleDefinitionActionParameter[] = [];
	readonly entityOptions = [...ENTITY_OPTIONS];
	readonly configEntityOptions = [...CONFIGURATION_ENTITY_OPTIONS];

	constructor(
		dialogRef: MatDialogRef<RuleDefinitionActionParameterDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: RuleDefinitionActionParameterDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		this.parameters = JSON.parse(JSON.stringify(this.data.parameters ?? []));
	}

	addParameter(): void {
		this.parameters = [...this.parameters, {
			id: '',
			label: '',
			dataEntity: undefined,
			configurationEntity: undefined,
			options: undefined,
			sortOrder: this.parameters.length
		}];
	}

	removeParameter(index: number): void {
		this.parameters = this.parameters.filter((_, i) => i !== index);
		this.reindexSortOrders();
	}

	moveUp(index: number): void {
		if(index === 0) {
			return;
		}
		const params = [...this.parameters];
		[params[index - 1], params[index]] = [params[index], params[index - 1]];
		this.parameters = params;
		this.reindexSortOrders();
	}

	moveDown(index: number): void {
		if(index === this.parameters.length - 1) {
			return;
		}
		const params = [...this.parameters];
		[params[index], params[index + 1]] = [params[index + 1], params[index]];
		this.parameters = params;
		this.reindexSortOrders();
	}

	private reindexSortOrders(): void {
		this.parameters.forEach((p, i) => p.sortOrder = i);
	}

	updateField(index: number, field: keyof RuleDefinitionActionParameter, value: any): void {
		this.parameters[index] = {...this.parameters[index], [field]: value};
	}

	isValid(): boolean {
		return this.parameters.every(p => !!p.id?.trim() && !!p.label?.trim());
	}

	onSave(): void {
		if(!this.isValid()) {
			return;
		}
		this.dialogRef.close(this.parameters);
	}
}
