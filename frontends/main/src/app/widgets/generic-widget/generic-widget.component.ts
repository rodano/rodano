import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CMSWidget} from '@core/model/cms-widget';
import {FieldModelCriterion} from '@core/model/field-model-criterion';
import {Scope} from '@core/model/scope';
import {WorkflowWidgetComponent} from '../workflow/workflow-widget.component';
import {ChartWidgetComponent} from '../chart/chart-widget.component';
import {ResourceWidgetComponent} from '../resource/resource-widget.component';
import {GeneralInfoWidgetComponent} from '../general-info/general-info-widget.component';
import {WorkflowSummaryWidgetComponent} from '../workflow-summary/workflow-summary-widget.component';
import {LockSummaryWidgetComponent} from '../lock-summary/lock-summary-widget.component';
import {OverdueComponent} from '../overdue/overdue.component';
import {WelcomeTextComponent} from '../welcome-text/welcome-text.component';

@Component({
	selector: 'app-generic-widget',
	templateUrl: './generic-widget.component.html',
	styleUrls: ['./generic-widget.component.css'],
	imports: [
		GeneralInfoWidgetComponent,
		ResourceWidgetComponent,
		ChartWidgetComponent,
		WorkflowWidgetComponent,
		WorkflowSummaryWidgetComponent,
		LockSummaryWidgetComponent,
		OverdueComponent,
		WelcomeTextComponent
	]
})
export class GenericWidgetComponent {
	@Input() widget: CMSWidget;
	@Input() scopes?: Scope[];
	@Input() criteria?: FieldModelCriterion[];

	@Output() notifyParent = new EventEmitter<number>();

	passEventToParent(event: number) {
		this.notifyParent.emit(event);
	}
}
