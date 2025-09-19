import {Component, Input} from '@angular/core';
import {CMSLayout} from '@core/model/cms-layout';
import {Scope} from '@core/model/scope';
import {LocalizeMapPipe} from '../../pipes/localize-map.pipe';
import {GenericWidgetComponent} from '../generic-widget/generic-widget.component';
import {FieldModelCriterion} from '@core/model/field-model-criterion';

@Component({
	selector: 'app-widget-layout-dto',
	templateUrl: './widget-layout.component.html',
	styleUrls: ['./widget-layout.component.css'],
	imports: [
		GenericWidgetComponent,
		LocalizeMapPipe
	]
})
export class WidgetLayoutComponent {
	@Input() layout: CMSLayout;
	@Input() rootScopes: Scope[];
	@Input() criteria?: FieldModelCriterion[];
}
