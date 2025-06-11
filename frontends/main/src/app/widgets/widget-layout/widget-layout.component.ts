import {Component, Input} from '@angular/core';
import {CMSLayoutDTO} from '@core/model/cms-layout-dto';
import {ScopeDTO} from '@core/model/scope-dto';
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
	@Input() layout: CMSLayoutDTO;
	@Input() rootScopes: ScopeDTO[];
	@Input() criteria?: FieldModelCriterion[];
}
