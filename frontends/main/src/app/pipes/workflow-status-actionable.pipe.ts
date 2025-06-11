import {Pipe, PipeTransform} from '@angular/core';
import {WorkflowStatusDTO} from '@core/model/workflow-status-dto';

@Pipe({
	name: 'actionableStatus'
})
export class WorkflowStatusActionablePipe implements PipeTransform {
	transform(statuses: WorkflowStatusDTO[]): WorkflowStatusDTO[] {
		return statuses.filter(status => status.state.possibleActions.length > 0);
	}
}
