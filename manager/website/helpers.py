import config
import logging
import json
import sys
import traceback
import tornado.web

logger = logging.getLogger(__name__)

def validate_token(token):
	return token == config.MAGIC_TOKEN

def validate_authorization_header(header):
	if not header:
		return False
	scheme, token = header.split(" ")
	if scheme != "Bearer":
		return False
	return validate_token(token)

def validate_websocket_header(header):
	if not header:
		return False
	scheme, token = header.split(", ")
	if scheme != "access_token":
		return False
	return validate_token(token.strip())

class JSONCustomEncoder(json.JSONEncoder):
	def default(self, object):
		if object.__class__.__name__ == "datetime":
			return object.isoformat() + "Z"
		if object.__class__.__name__ == "Row":
			json_object = {}
			for key in object.keys():
				json_object[key] = object[key]
			return json_object
		return json.JSONEncoder.default(self, object)

class CustomRequestHandler(tornado.web.RequestHandler):
	def set_default_headers(self):
		self.set_header("Content-Type", "application/json")

class AuthenticatedRequestHandler(CustomRequestHandler):
	def prepare(self):
		#using authorized key bypass other mechanism
		authorization = self.request.headers.get("Authorization")
		if not validate_authorization_header(authorization):
			self.set_status(401)
			self.write({"message" : "Invalid authorization"})
			self.finish()

	def _handle_request_exception(self, e):
		traceback.print_exc(file=sys.stdout)
		#retrieve exception message
		message = e.message if hasattr(e, "message") else str(e)
		logger.error(message)
		self.set_status(500)
		self.write({"error" : message})
		self.finish()
