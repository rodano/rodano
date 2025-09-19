import {Pipe, PipeTransform} from '@angular/core';
import {WorkflowStatus} from '@core/model/workflow-status';

@Pipe({
	name: 'notImportantStatus'
})
export class WorkflowStatusNotImportantPipe implements PipeTransform {
	transform(statuses: WorkflowStatus[]): WorkflowStatus[] {
		return statuses.filter(status => !status.state.important);
	}
}
