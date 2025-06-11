export interface ScheduledTask {
	expression: string;
	runnable: {target: string};
}
