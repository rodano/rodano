import {Pipe, PipeTransform} from '@angular/core';
import {WorkflowStatusDTO} from '@core/model/workflow-status-dto';

@Pipe({
	name: 'notImportantStatus'
})
export class WorkflowStatusNotImportantPipe implements PipeTransform {
	transform(statuses: WorkflowStatusDTO[]): WorkflowStatusDTO[] {
		return statuses.filter(status => !status.state.important);
	}
}
