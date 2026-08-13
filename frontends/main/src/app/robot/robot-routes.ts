import {Route} from '@angular/router';
import {signal} from '@angular/core';
import {Robot} from '@core/model/robot';
import {RobotComponent, ROBOT_TOKEN} from './home/robot.component';
import {RobotCreateComponent} from './create/robot-create.component';
import {RobotEditComponent} from './edit/robot-edit.component';
import {RobotResolver} from './resolvers/robot-resolver';
import {RobotListComponent} from './robot-list/robot-list.component';

export default [
	{
		path: '',
		component: RobotListComponent
	},
	{
		path: 'new',
		component: RobotComponent,
		providers: [
			{provide: ROBOT_TOKEN, useFactory: () => signal<Robot>(null as unknown as Robot)}
		],
		children: [
			{
				path: '**',
				component: RobotCreateComponent
			}
		]
	},
	{
		path: ':robotPk',
		component: RobotComponent,
		providers: [
			{provide: ROBOT_TOKEN, useFactory: () => signal<Robot>(null as unknown as Robot)}
		],
		resolve: {
			robot: RobotResolver
		},
		children: [
			{
				path: '**',
				component: RobotEditComponent
			}
		]
	}
] satisfies Route[];
