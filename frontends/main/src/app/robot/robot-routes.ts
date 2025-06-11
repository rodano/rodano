import {Route} from '@angular/router';
import {RobotComponent} from './home/robot.component';
import {RobotResolver} from './resolvers/robot-resolver';
import {RobotListComponent} from './robot-list/robot-list.component';

export default [
	{
		path: '',
		component: RobotListComponent
	},
	{
		path: 'new',
		component: RobotComponent
	},
	{
		path: ':robotPk',
		component: RobotComponent,
		resolve: {
			robot: RobotResolver
		}
	}
] satisfies Route[];
