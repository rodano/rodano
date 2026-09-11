import {Component, DestroyRef, OnInit, input, signal} from '@angular/core';
import {FormBuilder, Validators, ReactiveFormsModule} from '@angular/forms';
import {MeService} from '@core/services/me.service';
import {Router} from '@angular/router';
import {ScopeModel} from '@core/model/scope-model';
import {ScopeMini} from '@core/model/scope-mini';
import {ScopeService} from '@core/services/scope.service';
import {NotificationService} from '../../services/notification.service';
import {MatButton} from '@angular/material/button';
import {MatOption} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';
import {MatInput} from '@angular/material/input';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {ScopeCandidate} from '@core/model/scope-candidate';
import {ScopeCodeShortnamePipe} from '../../pipes/scope-code-shortname.pipe';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {Rights} from '@core/model/rights';
import {RightEntity} from '@core/enums/right-entity';

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
	readonly scopeModel = input.required<ScopeModel>();

	scopeCreationForm = this.formBuilder.group({
		code: this.formBuilder.nonNullable.control('', [Validators.required]),
		shortname: this.formBuilder.nonNullable.control('', [Validators.required]),
		parentScopePk: this.formBuilder.control<number | undefined>(undefined, [Validators.required])
	});

	readonly parentScopes = signal<ScopeMini[]>([]);

	constructor(
		private router: Router,
		private formBuilder: FormBuilder,
		private meService: MeService,
		private scopeService: ScopeService,
		private notificationService: NotificationService,
		private destroyRef: DestroyRef
	) { }

	ngOnInit() {
		this.meService.getScopesForRequiredRight(RightEntity.SCOPE_MODEL, this.scopeModel().id, Rights.READ, this.scopeModel().parentIds).subscribe(s => this.parentScopes.set(s));
	}

	save() {
		const scopeCandidate = Object.assign({}, this.scopeCreationForm.value) as ScopeCandidate;
		scopeCandidate.modelId = this.scopeModel().id;
		//Set the scope start date to now
		scopeCandidate.startDate = new Date();
		this.scopeService.create(scopeCandidate).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: scope => {
				this.notificationService.showSuccess(`${this.scopeModel().shortname['en']} created`);
				this.router.navigate([
					'/scopes',
					scope.modelId,
					scope.pk
				]);
			},
			error: response => {
				console.error(response);
				this.notificationService.showError(`Failed to create ${this.scopeModel().shortname['en']}: ${response.error.message}`);
			}
		});
	}
}
