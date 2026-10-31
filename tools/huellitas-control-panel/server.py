#!/usr/bin/env python3
"""LAN-only control panel for the two Huellitas development services."""

from __future__ import annotations

import hmac
import ipaddress
import json
import os
import socket
import subprocess
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlsplit


TOKEN = os.environ.get("HUELLITAS_PANEL_TOKEN", "")
HOST = os.environ.get("HUELLITAS_PANEL_HOST", "0.0.0.0")
PORT = int(os.environ.get("HUELLITAS_PANEL_PORT", "8765"))
SERVICES = {
    "spring": {"unit": "huellitas-spring.service", "port": 8080},
    "angular": {"unit": "huellitas-angular.service", "port": 4200},
}
ROOT = Path(__file__).resolve().parent


def systemctl_command(service: str, action: str) -> list[str]:
    """Map a fixed UI action to a fixed user unit; never accept shell input."""
    if service not in SERVICES or action not in {"start", "stop", "restart"}:
        raise ValueError("Servicio o acción no permitidos")
    return ["systemctl", "--user", action, SERVICES[service]["unit"]]


def port_is_open(port: int) -> bool:
    try:
        with socket.create_connection(("127.0.0.1", port), timeout=0.25):
            return True
    except OSError:
        return False


def service_status(service: str) -> dict[str, str | int]:
    config = SERVICES[service]
    result = subprocess.run(
        ["systemctl", "--user", "is-active", config["unit"]],
        capture_output=True,
        text=True,
        timeout=5,
        check=False,
    )
    state = result.stdout.strip() or "inactive"
    if state == "inactive" and port_is_open(config["port"]):
        state = "external"
    return {"state": state, "port": config["port"]}


class ControlHandler(BaseHTTPRequestHandler):
    server_version = "HuellitasPanel/1.0"

    def log_message(self, format: str, *args: object) -> None:
        # Do not log authorization headers or request details.
        print(f"[huellitas-panel] {self.address_string()} {self.command}")

    def end_headers(self) -> None:
        self.send_header("X-Content-Type-Options", "nosniff")
        self.send_header("X-Frame-Options", "DENY")
        self.send_header("Referrer-Policy", "no-referrer")
        self.send_header(
            "Content-Security-Policy",
            "default-src 'self'; style-src 'self'; script-src 'self'; "
            "connect-src 'self'; "
            "img-src 'self' data:; frame-ancestors 'none'; base-uri 'none'",
        )
        super().end_headers()

    def _json(self, status: int, payload: dict[str, object]) -> None:
        data = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Cache-Control", "no-store")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def _authorized(self) -> bool:
        client_ip = self.client_address[0]
        try:
            if not (ipaddress.ip_address(client_ip).is_private or ipaddress.ip_address(client_ip).is_loopback):
                return False
        except ValueError:
            return False
        supplied = self.headers.get("Authorization", "")
        return supplied.startswith("Bearer ") and hmac.compare_digest(
            supplied[7:].encode("utf-8"), TOKEN.encode("utf-8")
        )

    def do_GET(self) -> None:
        path = urlsplit(self.path).path
        static_files = {
            "/": ("index.html", "text/html; charset=utf-8", "no-store"),
            "/panel.css": ("panel.css", "text/css; charset=utf-8", "no-cache"),
            "/panel.js": ("panel.js", "text/javascript; charset=utf-8", "no-cache"),
        }
        if path in static_files:
            filename, content_type, cache_control = static_files[path]
            page = (ROOT / filename).read_bytes()
            self.send_response(200)
            self.send_header("Content-Type", content_type)
            self.send_header("Cache-Control", cache_control)
            self.send_header("Content-Length", str(len(page)))
            self.end_headers()
            self.wfile.write(page)
            return
        if path != "/api/status":
            self._json(404, {"error": "No encontrado"})
            return
        if not self._authorized():
            self._json(401, {"error": "Token incorrecto o acceso fuera de la red local"})
            return
        try:
            self._json(200, {name: service_status(name) for name in SERVICES})
        except (OSError, subprocess.SubprocessError) as error:
            self._json(503, {"error": f"No se pudo consultar systemd: {error}"})

    def do_POST(self) -> None:
        if not self._authorized():
            self._json(401, {"error": "Token incorrecto o acceso fuera de la red local"})
            return
        parts = urlsplit(self.path).path.strip("/").split("/")
        if len(parts) != 4 or parts[:2] != ["api", "services"]:
            self._json(404, {"error": "No encontrado"})
            return
        _, _, service, action = parts
        if service not in SERVICES or action not in {"start", "stop", "restart"}:
            self._json(400, {"error": "Servicio o acción no permitidos"})
            return
        try:
            if action == "start" and service_status(service)["state"] == "external":
                self._json(409, {
                    "error": f"El puerto {SERVICES[service]['port']} ya lo ocupa un proceso iniciado fuera del panel. Detén ese proceso una vez y vuelve a intentarlo."
                })
                return
            result = subprocess.run(
                systemctl_command(service, action),
                capture_output=True,
                text=True,
                timeout=35,
                check=False,
            )
        except subprocess.TimeoutExpired:
            self._json(504, {"error": "systemd tardó demasiado en responder"})
            return
        except subprocess.SubprocessError:
            self._json(503, {"error": "No se pudo consultar el estado de systemd"})
            return
        except OSError:
            self._json(503, {"error": "No se pudo ejecutar systemctl --user"})
            return
        if result.returncode != 0:
            self._json(500, {"error": result.stderr.strip() or "systemd no pudo completar la acción"})
            return
        self._json(200, {"ok": True, "service": service, "action": action, "status": service_status(service)})


def main() -> None:
    if len(TOKEN) < 32:
        raise SystemExit("Configura HUELLITAS_PANEL_TOKEN con al menos 32 caracteres.")
    server = ThreadingHTTPServer((HOST, PORT), ControlHandler)
    server.daemon_threads = True
    print(f"Panel local escuchando en {HOST}:{PORT}")
    server.serve_forever()


if __name__ == "__main__":
    main()
