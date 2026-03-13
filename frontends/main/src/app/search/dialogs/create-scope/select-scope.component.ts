import {ChangeDetectionStrategy, Component, Inject, signal} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {Scope} from '@core/model/scope';
import {MatButton} from '@angular/material/button';
import {MatFormField, MatLabel, MatOption, MatSelect} from '@angular/material/select';
import {ScopeModel} from '@core/model/scope-model';
import {LocalizeMapPipe} from 'src/app/pipes/localize-map.pipe';
import {LowerCasePipe} from '@angular/common';
import {ScopeCodeShortnamePipe} from 'src/app/pipes/scope-code-shortname.pipe';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	selector: 'app-select-scope',
	templateUrl: './select-scope.component.html',
	imports: [
		MatDialogModule,
		MatButton,
		MatSelect,
		MatFormField,
		MatLabel,
		MatOption,
		LocalizeMapPipe,
		LowerCasePipe,
		ScopeCodeShortnamePipe
	]
})
export class SelectScopeComponent {
	readonly parentScopes = signal<Scope[]>([]);
	selectedScope: Scope;

	constructor(
		@Inject(MAT_DIALOG_DATA) public data: {parentScopes: Scope[]; childScopeModel: ScopeModel}
	) {
		this.parentScopes.set(this.data.parentScopes);
	}
}
