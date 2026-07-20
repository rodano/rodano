import enum
import json
import logging
import tornado.websocket
import tasks
import helpers

logger = logging.getLogger(__name__)

class MessageType(enum.Enum):
	LOG = "LOG"
	TASK = "TASK"

messaging_clients = []

def broadcast_task_message(data):
	data["type"] = MessageType.TASK.value
	broadcast_message(data)

def broadcast_message(data):
	global messaging_clients
	message = json.dumps(data)
	for client in messaging_clients:
		try:
			client.write_message(message)
		except:
			logger.error(f"Unable to send message: {message}", exc_info=True)

class Messaging(tornado.websocket.WebSocketHandler):
	def select_subprotocol(self, subprotocols):
		if "access_token" in subprotocols:
			return "access_token"
		return None

	def open(self):
		global messaging_clients
		if not helpers.validate_websocket_header(self.request.headers.get("Sec-WebSocket-Protocol")):
			self.close(code=4001, reason="Unauthorized")
		if self not in messaging_clients:
			messaging_clients.append(self)

	def on_message(self, message):
		if message == "status":
			#send information about running tasks to client
			for (type, task) in tasks.tasks.items():
				if task is not None:
					message = {"type": MessageType.TASK.value, "task" : type, "id" : task["id"], "running" : True}
					self.write_message(message)
		else:
			self.write_message({"error" : "Invalid command"})

	def on_close(self):
		global messaging_clients
		if self in messaging_clients:
			messaging_clients.remove(self)

url_pattern = [
	("/api/messaging", Messaging),
]
