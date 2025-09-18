import {bus} from './event_bus.js';
import {api_base_url, Fetch} from './fetch.js';

const tasks_text = {
	control: 'Action',
	backup: 'Backup',
	restore: 'Restoration',
	reset: 'Reset'
};

function cancel_task_listener(event) {
	event.stop();
	//update ui
	const parent = this.parentNode;
	parent.removeChild(this);
	parent.textContent = 'Asking to cancel task.';
	//execute request
	Fetch(`${api_base_url}/task/${this.dataset.taskId}`, {'method': 'DELETE', 'managed-error': [404]})
		.then(result => {
			//task may be terminated by the request
			//in this case, remove notification
			if(result.terminated) {
				parent.remove();
			}
			//task may have been asked to be terminated
			//in this case, the task it self will send a message when it will be terminated
			else {
				parent.textContent = 'Task is being cancelled.';
			}
		})
		.catch(() => {
			//the task may have already been terminated
			parent.remove();
		});
}

export const Tasks = {
	Init: function() {
		bus.register({
			onTaskMessage: function(event) {
				const message = event.message;
				if(message.hasOwnProperty('begin') || message.hasOwnProperty('running') || message.hasOwnProperty('end')) {
					const tasks = document.getElementById('tasks');
					if(message.begin || message.running) {
						//create notification if it does not already exists
						//the notification may already exists if socket connection has been closed unexpectedly and re-opened automatically (thanks to function reconnect_socket)
						//or if the task has been started by another client
						if(!tasks.querySelector(`div[data-task-id="${message.id}"]`)) {
							const task = document.createFullElement('div', {'data-task-id': message.id});
							task.appendChild(document.createTextNode(`${tasks_text[message.task]} triggered.`));
							task.appendChild(document.createFullElement('button', {'data-task-id': message.id, 'style': 'margin-left: 1rem;'}, 'Cancel task', {click: cancel_task_listener}));
							tasks.appendChild(task);
						}
					}
					else if(message.end) {
						tasks.removeChild(tasks.querySelector(`div[data-task-id="${message.id}"]`));
					}
				}
			}
		});
	}
};
