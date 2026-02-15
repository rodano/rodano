import {Component, EventEmitter, Input, OnChanges, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {TreeNode} from '../tree-node';
import {LanguageService} from '../../services/language.service';
import {ValidatorService} from '../../services/api/validator.service';

@Component({
	selector: 'app-validator-tree',
	standalone: true,
	imports: [CommonModule, MatIconModule],
	templateUrl: './validator-tree.component.html',
	styleUrls: ['../tree-shared.css']
})
export class ValidatorTreeComponent implements OnInit, OnChanges {
	@Input() projectId = '';
	@Input() expanded = false;
	@Input() selectedValidatorId: string | null = null;
	@Output() categoryClicked = new EventEmitter<void>();

	validators: any[] = [];
	treeNodes: TreeNode[] = [];

	constructor(
		private validatorService: ValidatorService,
		private languageService: LanguageService
	) {}

	ngOnInit(): void {
		this.loadValidators();
	}

	ngOnChanges(): void {
		this.buildTree();
	}

	loadValidators(): void {
		this.validatorService.getValidators(this.projectId).subscribe({
			next: validators => {
				this.validators = validators;
				this.buildTree();
			},
			error: error => console.error('Error loading validators:', error)
		});
	}

	onCategoryClick(): void {
		this.categoryClicked.emit();
	}

	private buildTree(): void {
		if(!this.validators.length) {
			return;
		}

		this.treeNodes = this.validators.map(v => {
			const node: TreeNode = {
				id: `validator-${v.validatorId}`,
				label: this.languageService.getDefaultTranslation(v.shortname) || v.id,
				icon: 'check_circle',
				type: 'dataset-model',
				selected: this.selectedValidatorId === v.validatorId,
				entityId: v.validatorId
			};

			return node;
		});
	}

	reload(): void {
		this.loadValidators();
	}
}
