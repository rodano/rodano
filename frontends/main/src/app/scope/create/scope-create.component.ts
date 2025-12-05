import {Component, DestroyRef, Input, OnInit} from '@angular/core';
import {FormBuilder, Validators, ReactiveFormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {ScopeModel} from '@core/model/scope-model';
import {Scope} from '@core/model/scope';
import {ScopeService} from '@core/services/scope.service';
import {NotificationService} from 'src/app/services/notification.service';
import {ScopeRelationsService} from '@core/services/scope-relations.service';
import {MatButton} from '@angular/material/button';
import {MatOption} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';
import {MatInput} from '@angular/material/input';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {ScopeCandidate} from '@core/model/scope-candidate';
import {ScopeCodeShortnamePipe} from 'src/app/pipes/scope-code-shortname.pipe';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {Rights} from '@core/model/rights';

@Component({
	templateUrl: './scope-create.component.html',
	styleUrls: ['./scope-create.component.css'],
	imports: [
		ReactiveFormsModule,
		MatLabel,
		MatFormField,
		MatSelect,
		MatOption,
		MatInput,
		MatButton,
		ScopeCodeShortnamePipe
	]
})
export class ScopeCreateComponent implements OnInit {
	@Input() scopeModel: ScopeModel;

	scopeCreationForm = this.formBuilder.nonNullable.group({
		code: ['', [Validators.required]],
		shortname: ['', [Validators.required]],
		parentScopePk: [1, [Validators.required]]
	});

	parentScopes: Scope[];

	constructor(
		private router: Router,
		private formBuilder: FormBuilder,
		private scopeService: ScopeService,
		private scopeRelationsService: ScopeRelationsService,
		private notificationService: NotificationService,
		private destroyRef: DestroyRef
	) { }

	ngOnInit() {
		this.scopeRelationsService.getParents(this.scopeModel.scopeModelId, Rights.READ).subscribe(s => this.parentScopes = s);
	}

	save() {
		const scopeCandidate = Object.assign({}, this.scopeCreationForm.value) as ScopeCandidate;
		scopeCandidate.modelId = this.scopeModel.scopeModelId;
		//Set the scope start date to now
		scopeCandidate.startDate = new Date();
		this.scopeService.create(scopeCandidate).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(s => {
			this.notificationService.showSuccess(`${this.scopeModel.shortname['en']} created`);
			this.router.navigate([
				'/scopes',
				s.modelId,
				s.pk
			]);
		});
	}
}
