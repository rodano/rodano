import {Component, Input, OnChanges} from '@angular/core';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {ScopeDTO} from '@core/model/scope-dto';
import {ScopeModelDTO} from '@core/model/scope-model-dto';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {MatTabsModule} from '@angular/material/tabs';
import {MatIcon} from '@angular/material/icon';
import {MatIconButton} from '@angular/material/button';
import {LowerCasePipe} from '@angular/common';
import {FormService} from '@core/services/form.service';
import {FormDTO} from '@core/model/form-dto';
import {MatTooltip} from '@angular/material/tooltip';
import {AuditTrailButtonComponent} from 'src/app/audit-trail-button/audit-trail-button.component';

@Component({
	templateUrl: './scope.component.html',
	styleUrls: ['./scope.component.css'],
	imports: [
		MatIconButton,
		RouterLink,
		MatIcon,
		MatTooltip,
		MatTabsModule,
		RouterLinkActive,
		RouterOutlet,
		LowerCasePipe,
		LocalizeMapPipe,
		AuditTrailButtonComponent
	]
})
export class ScopeComponent implements OnChanges {
	@Input() scopeModel: ScopeModelDTO;
	//scope will be undefined when this component is displayed to create a new scope
	@Input() scope?: ScopeDTO;

	forms: FormDTO[] = [];

	constructor(
		private formService: FormService
	) {}

	ngOnChanges() {
		if(this.scope) {
			this.formService.searchOnScope(this.scope.pk).subscribe(forms => {
				this.forms = forms;
			});
		}
	}
}
