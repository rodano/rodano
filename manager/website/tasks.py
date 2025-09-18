import logging
import asyncio
import messaging
import helpers
import config

logger = logging.getLogger(__name__)

#tasks management
tasks = {
	"control" : None,
	"backup" : None,
	"reset" : None,
	"restore" : None
}

def quote_argument(argument):
	if argument is None or not argument:
		return "''"
	if argument is not None and " " in argument:
		return f"'{argument}'"
	return argument

#execute task relying on an external bash script
async def do_task(task, steps, *args):
	global tasks
	messaging.broadcast_task_message({"task" : task, "id" : tasks[task]["id"], "begin" : True})

	#launch process
	parameters = []
	parameters.append(config.get_script_path(task))
	#protect arguments
	arguments = map(quote_argument, args)
	parameters.extend(arguments)
	#prepare command
	command = " ".join(parameters)
	logger.info(f"Executing command {command}")
	process = await asyncio.create_subprocess_shell(
		command,
		stdout=asyncio.subprocess.PIPE,
		stderr=asyncio.subprocess.PIPE,
		close_fds=True
	)

	#link process to task
	tasks[task]["process"] = process

	success = False

	#report to connected clients
	while process.returncode is None:
		try:
			#if process has been killed, readline will never return, hence the timeout
			#if process is still alive and no line has been return, process.returncode will still be None and the while loop will continue
			line = await asyncio.wait_for(process.stdout.readline(), 3)
		except asyncio.TimeoutError:
			pass
		else:
			line = line.strip().decode("utf-8")
			if line:
				logger.info(line)
				data = {"task" : task, "id" : tasks[task]["id"], "message" : line}
				for index, step in enumerate(steps):
					if step in line:
						data["step"] = index + 1
						if data["step"] == len(steps):
							success = True
						break
				messaging.broadcast_task_message(data)

	if process.returncode == 0:
		#retrieve all content of stdout in case we missed some bits
		streams = await process.communicate()
		out = streams[0].decode("utf-8")

		#check if success message appears in last chunk of stdout
		if steps[-1] in out:
			success = True
			messaging.broadcast_task_message({"task" : task, "id" : tasks[task]["id"], "message" : out, "step" : len(steps)})

		if success:
			messaging.broadcast_task_message({"task" : task, "id" : tasks[task]["id"], "end" : True, "message" : f"End of {task}."})
		else:
			#return error and send last message from stderr if any
			message = {"task" : task, "id" : tasks[task]["id"], "end" : True, "error" : f"Error while executing {task}."}
			error = streams[1].decode("utf-8")
			if error:
				message["details"] = error
			messaging.broadcast_task_message(message)
	else:
		messaging.broadcast_task_message({"task" : task, "id" : tasks[task]["id"], "end" : True, "error" : f"Failed to execute {task}. Process exit code is {process.returncode}"})

	#free task process
	tasks[task] = None

	#return success
	return success

class TaskCancel(helpers.AuthenticatedRequestHandler):
	def delete(self, id):
		#find good task
		global tasks
		for task_type, task in tasks.items():
			if task is not None and task["id"] == id:
				#terminate task if it is linked to a process
				if "process" in task:
					try:
						#this sends a SIGTERM to the process
						task["process"].terminate()
						#do not remove task from task list
						#this will be handle by the code that started the task
						messaging.broadcast_task_message({"task" : task_type, "id" : id, "cancelled" : True, "message" : "Task cancelled."})
						self.write({"message" : "Task cancelled.", "terminated" : False})
					except OSError:
						tasks[task_type] = None
						messaging.broadcast_task_message({"task" : task_type, "id" : id, "end" : True, "error" : "Task died."})
						self.write({"message" : "Task died.", "terminated" : True})
				else:
					tasks[task_type] = None
					messaging.broadcast_task_message({"task" : task_type, "id" : id, "end" : True, "error" : "Task terminated."})
					self.write({"error" : "Task terminated.", "terminated" : True})
				return
		#no task has been found
		self.set_status(404)
		self.write({"error" : f"There is no running task with id {id}."})

url_pattern = [
	(r"/api/task/([a-f0-9\-]{36})", TaskCancel)
]
