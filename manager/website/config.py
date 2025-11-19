import os
import configparser

config = configparser.ConfigParser()
config.read(os.path.join(os.path.dirname(__file__), "config.ini"))

DEBUG = os.getenv("DEBUG", "false").lower() == "true"
DEBUG_PATH = os.path.join(os.path.dirname(__file__), "../scripts")

def get_script_path(script_name):
	base = DEBUG_PATH if DEBUG else config.get("DEFAULT", "base")
	return f"{base}/{config.get('scripts', script_name)}"
