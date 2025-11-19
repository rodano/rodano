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

logger = logging.getLogger(__name__)

DEBUG = os.getenv("DEBUG", "false").lower() == "true"

magic_token = os.getenv("MAGIC_TOKEN")
base_url_path = os.getenv("BASE_URL_PATH", "/")

static_path = os.path.join(os.path.dirname(os.path.realpath(__file__)), "static")

class IndexHandler(tornado.web.RequestHandler):
	def get(self):
		self.render(os.path.join(static_path, "index.html"), base_url_path=base_url_path)

url_pattern = []
url_pattern.extend(messaging.url_pattern)
url_pattern.extend(tasks.url_pattern)
url_pattern.extend(applications.url_pattern)
url_pattern.extend([
	("/", IndexHandler),
	("/(.+)", tornado.web.StaticFileHandler, {"path": static_path})
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
		logger.error("The MAGIC_TOKEN environment must be set to start the manager")
		exit(1)

	if not base_url_path.startswith("/") or not base_url_path.endswith("/"):
		logger.error("The BASE_URL_PATH environment variable must start and end with a /")
		exit(1)

	logger.info(f"Base path set to: {base_url_path}")

	asyncio.run(main())
