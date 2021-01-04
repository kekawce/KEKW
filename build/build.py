import concurrent.futures
import os
import subprocess
import sys
import threading
from pathlib import Path
from shutil import copy2

from settings import ALIAS, DESTS, ENV_VARS, KEYPASS, KEYSTORE, STOREPASS


def set_env_vars_and_execute_cmd(cmd, path, env_vars=None):
    if env_vars is None:
        env_vars = []

    with subprocess.Popen(
        "cmd.exe", stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE
    ) as process:
        with process.stdin as stdin:
            stdin.write(f"set PATH={ENV_VARS[path]}\n".encode())
            for var in env_vars:
                stdin.write(f"set {var}={ENV_VARS[var]}\n".encode())
            stdin.flush()

            stdin.write(cmd)
            stdin.flush()

        with process.stdout as stdout:
            for line in iter(stdout.readline, b""):
                print(line.decode().rstrip())


set_env_vars_and_execute_cmd(b"mvn clean package\n", "M2", ["JAVA_HOME"])

jarfile = next((Path.cwd() / "target").glob("*jar"), None)
if jarfile is None:
    print("couldnt find jar file")
    sys.exit()

jarsigner_cmd = (
    "jarsigner "
    f"-keystore {KEYSTORE} "
    f"-storepass {STOREPASS} "
    f"-keypass {KEYPASS} "
    f"{jarfile} {ALIAS}\n"
).encode()
set_env_vars_and_execute_cmd(jarsigner_cmd, "JAVA")

for dest in DESTS:
    print(f"copying {os.path.basename(jarfile)} to {dest}")
    copy2(jarfile, dest)
