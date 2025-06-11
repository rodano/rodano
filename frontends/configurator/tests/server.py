#!/usr/bin/python
import sys
import os
from http.server import BaseHTTPRequestHandler, HTTPServer

if len(sys.argv) < 2:
	print("Specify file to serve")
	exit()

#retrieve file to serve as first parameter
FILE_PATH = sys.argv[1]

if not os.path.isfile(FILE_PATH):
	print("Invalid file")
	exit()

class customHandler(BaseHTTPRequestHandler):

	def do_GET(self):
		self.send_response(200)
		self.send_header("Access-Control-Allow-Origin", "*")
		self.send_header("Content-type", "application/json")
		self.end_headers()
		with open(FILE_PATH, "rb") as f:
			self.wfile.write(f.read())
		return

try:
	server = HTTPServer(("", 80), customHandler)
	print("Starting server")
	server.serve_forever()

except KeyboardInterrupt:
	print("Stopping server")
	server.socket.close()
