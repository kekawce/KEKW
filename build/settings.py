import os
from os.path import dirname, join

from dotenv import dotenv_values

ENV_VARS = dotenv_values(join(dirname(__file__), ".env"))

KEYSTORE = ENV_VARS["KEYSTORE"]
ALIAS = ENV_VARS["ALIAS"]
STOREPASS = ENV_VARS["STOREPASS"]
KEYPASS = ENV_VARS["KEYPASS"]

DESTS = [val for key, val in ENV_VARS.items() if key.startswith("DEST")]
