import {Pipe, PipeTransform} from '@angular/core';
import {WorkflowStatus} from '@core/model/workflow-status';

@Pipe({
	name: 'actionableStatus'
})
export class WorkflowStatusActionablePipe implements PipeTransform {
	transform(statuses: WorkflowStatus[]): WorkflowStatus[] {
		return statuses.filter(status => status.state.possibleActions.length > 0);
	}
}
