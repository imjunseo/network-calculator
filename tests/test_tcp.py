"""Start the real Java server and exercise its public TCP protocol."""

from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
import socket
import subprocess
import tempfile
import time
import unittest


ROOT = Path(__file__).resolve().parents[1]


class CalculatorTcpTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        work = tempfile.TemporaryDirectory()
        cls.addClassCleanup(work.cleanup)
        sources = sorted(ROOT.glob("*.java"))
        subprocess.run(["javac", "--release", "17", "-d", work.name, *map(str, sources)],
                       check=True, timeout=30)
        log_path = Path(work.name) / "server.log"
        with log_path.open("w") as log:
            server = subprocess.Popen(
                ["java", "-cp", work.name, "myfirstnetworkapp.calculator_server", "0"],
                stdout=log, stderr=subprocess.STDOUT,
            )
        cls.addClassCleanup(cls.stop_server, server)
        deadline = time.monotonic() + 10
        while server.poll() is None and time.monotonic() < deadline:
            log = log_path.read_text(errors="replace")
            first_line = log.partition("\n")[0]
            if first_line.startswith("Listening on port ") and "\n" in log:
                cls.port = int(first_line.rsplit(" ", 1)[1])
                return
            time.sleep(0.05)
        raise RuntimeError(f"Server failed to start: {log_path.read_text(errors='replace')}")

    @staticmethod
    def stop_server(server):
        if server.poll() is None:
            server.terminate()
            try:
                server.wait(timeout=5)
            except subprocess.TimeoutExpired:
                server.kill()
                server.wait(timeout=5)

    def request(self, command):
        with socket.create_connection(("127.0.0.1", self.port), timeout=10) as client:
            client.sendall((command + "\n").encode("ascii"))
            with client.makefile("rb") as reply:
                return tuple(reply.read().decode("ascii").splitlines())

    def test_operations_and_integer_overflow(self):
        for command, value in {
            "ADD 10 20": "30.0", "SUB 3 9": "-6.0", "MUL -3 8": "-24.0",
            "DIV 7 2": "3.5", "ADD 2147483647 1": "2.147483648E9",
        }.items():
            with self.subTest(command=command):
                self.assertEqual(self.request(command), ("STATUS: 200", f"VALUE: {value}"))

    def test_bad_requests(self):
        self.assertEqual(self.request("DIV 10 0"), ("STATUS: 400", "ERROR: divided by zero"))
        for command in ("ADD 1", "ADD x 1", "POW 2 3", "ADD 1 2 3"):
            with self.subTest(command=command):
                self.assertEqual(self.request(command),
                                 ("STATUS: 401", "ERROR: Invalid command or operands"))

    def test_concurrent_clients(self):
        with ThreadPoolExecutor(max_workers=12) as clients:
            results = list(clients.map(self.request, (f"ADD {i} 7" for i in range(24))))
        self.assertEqual(results, [("STATUS: 200", f"VALUE: {i + 7}.0") for i in range(24)])

    def test_open_client_does_not_block_others(self):
        with socket.create_connection(("127.0.0.1", self.port), timeout=8) as idle:
            self.assertEqual(self.request("MUL 6 7"), ("STATUS: 200", "VALUE: 42.0"))
            self.assertEqual(idle.recv(1), b"")


if __name__ == "__main__":
    unittest.main()
