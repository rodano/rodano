import {ChangeDetectionStrategy, Component, InjectionToken, OnInit, WritableSignal, inject, input, signal} from '@angular/core';
import {ActivatedRoute, RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {Scope} from '@core/model/scope';
import {ScopeModel} from '@core/model/scope-model';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {MatTabsModule} from '@angular/material/tabs';
import {MatIcon} from '@angular/material/icon';
import {MatIconButton} from '@angular/material/button';
import {LowerCasePipe} from '@angular/common';
import {FormService} from '@core/services/form.service';
import {Form} from '@core/model/form';
import {MatTooltip} from '@angular/material/tooltip';
import {AuditTrailButtonComponent} from 'src/app/audit-trail-button/audit-trail-button.component';

export const SCOPE_TOKEN = new InjectionToken<WritableSignal<Scope>>('scope');

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
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
export class ScopeComponent implements OnInit {
	//scope is a shared signal so child components can update it and the parent template reacts
	readonly scope = inject(SCOPE_TOKEN);
	readonly scopeModel = input.required<ScopeModel>();

	readonly forms = signal<Form[]>([]);

	constructor(
		private formService: FormService,
		private route: ActivatedRoute
	) {}

	ngOnInit() {
		const scope: Scope | undefined = this.route.snapshot.data['scope'];
		if(scope) {
			this.scope.set(scope);
			this.formService.searchOnScope(scope.pk).subscribe(forms => {
				this.forms.set(forms);
			});
		}
	}
}
