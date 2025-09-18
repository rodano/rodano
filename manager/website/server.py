import os
import logging
import asyncio
import tornado.web
import tornado.httpserver
import messaging
import tasks
import applications

logging.basicConfig(
	level=logging.INFO,
	format="%(asctime)s [%(levelname)s] %(message)s",
	datefmt="%Y-%m-%d %H:%M:%S",
	handlers=[
		logging.StreamHandler()
	]
)

DEBUG = os.getenv("DEBUG", "false").lower() == "true"

magic_token = os.getenv("MAGIC_TOKEN")
base_path = os.path.dirname(os.path.realpath(__file__))

url_pattern = []
url_pattern.extend(messaging.url_pattern)
url_pattern.extend(tasks.url_pattern)
url_pattern.extend(applications.url_pattern)
url_pattern.extend([
	("/", tornado.web.RedirectHandler, {"url": "index.html"}),
	("/(.+)", tornado.web.StaticFileHandler, {"path": os.path.join(base_path, "static")})
])

application = tornado.web.Application(url_pattern, debug=DEBUG)

async def main():
	#allow files of 500M to be uploaded
	server = tornado.httpserver.HTTPServer(application, max_buffer_size=500 * 1024 * 1024)
	server.listen(8081)

	applications.start_log_streaming()
	applications.start_periodic_backups()

	await asyncio.Event().wait()

if __name__ == "__main__":

	if not magic_token:
		print("The MAGIC_TOKEN environment must be set to start the manager")
		exit(1)

	asyncio.run(main())
