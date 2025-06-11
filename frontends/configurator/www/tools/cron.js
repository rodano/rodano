export class Cron {
	constructor(beat) {
		this.beat = beat || (1000 * 10);
		this.tasks = [];
		setInterval(() => {
			const now = new Date().getTime();
			this.tasks.forEach(function(task) {
				if(now - task.last_run > task.interval) {
					task.task.call();
					task.last_run = now;
				}
			});
		}, this.beat);
	}
	addTask(task, interval) {
		this.tasks.push({
			task: task,
			interval: interval,
			last_run: new Date().getTime()
		});
	}
	removeTask(task) {
		const index = this.tasks.findIndex(t => t.task === task);
		if(index >= 0) {
			this.tasks.remove(index);
		}
		else {
			throw new Error('No task found');
		}
	}
	clear() {
		this.tasks = [];
	}
}
