import {ChangeDetectionStrategy, Component, input} from '@angular/core';
import {CMSLayout} from '@core/model/cms-layout';
import {Scope} from '@core/model/scope';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {GenericWidgetComponent} from '../generic-widget/generic-widget.component';
import {FieldModelCriterion} from '@core/model/field-model-criterion';

@Component({
	changeDetection: ChangeDetectionStrategy.OnPush,
	selector: 'app-widget-layout-dto',
	templateUrl: './widget-layout.component.html',
	styleUrls: ['./widget-layout.component.css'],
	imports: [
		GenericWidgetComponent,
		LocalizeMapPipe
	]
})
export class WidgetLayoutComponent {
	readonly layout = input.required<CMSLayout>();
	readonly rootScopes = input.required<Scope[]>();
	readonly criteria = input<FieldModelCriterion[]>();
}
