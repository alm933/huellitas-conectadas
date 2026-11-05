#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd -- "$SCRIPT_DIR/../.." && pwd)"
CONFIG_DIR="$HOME/.config/huellitas"
SYSTEMD_DIR="$HOME/.config/systemd/user"
PANEL_ENV="$CONFIG_DIR/control-panel.env"

if [[ ! -x "$PROJECT_DIR/mvnw" || ! -f "$PROJECT_DIR/frontend/package.json" ]]; then
  echo "No encuentro el backend y frontend de Huellitas desde $PROJECT_DIR." >&2
  exit 1
fi
if [[ ! -f "$HOME/.config/huellitas/garage.env" ]]; then
  echo "Falta $HOME/.config/huellitas/garage.env, requerido por Spring." >&2
  exit 1
fi
NPM_BIN="$(command -v npm || true)"
NODE_BIN="$(command -v node || true)"
if [[ -z "$NPM_BIN" || -z "$NODE_BIN" ]]; then
  echo "No encuentro npm y node en el PATH de esta sesión. Ejecuta el instalador desde tu shell normal." >&2
  exit 1
fi

mkdir -p "$CONFIG_DIR" "$SYSTEMD_DIR"
chmod 700 "$CONFIG_DIR"
if [[ ! -f "$PANEL_ENV" ]]; then
  TOKEN="$(python3 -c 'import secrets; print(secrets.token_urlsafe(36))')"
  {
    printf 'HUELLITAS_PANEL_TOKEN=%s\n' "$TOKEN"
    printf 'HUELLITAS_PANEL_HOST=0.0.0.0\n'
    printf 'HUELLITAS_PANEL_PORT=8765\n'
  } > "$PANEL_ENV"
  chmod 600 "$PANEL_ENV"
else
  echo "Conservo el token de control ya existente en $PANEL_ENV."
fi

NODE_DIR="$(dirname -- "$NODE_BIN")"
cat > "$SYSTEMD_DIR/huellitas-spring.service" <<UNIT
[Unit]
Description=Huellitas Conectadas Spring Boot
After=network-online.target

[Service]
Type=simple
WorkingDirectory=$PROJECT_DIR
ExecStart=/bin/bash -c 'set -a; source "$HOME/.config/huellitas/garage.env"; set +a; exec ./mvnw spring-boot:run'
Restart=on-failure
RestartSec=5
TimeoutStopSec=30

[Install]
WantedBy=default.target
UNIT

cat > "$SYSTEMD_DIR/huellitas-angular.service" <<UNIT
[Unit]
Description=Huellitas Conectadas Angular
After=network-online.target

[Service]
Type=simple
WorkingDirectory=$PROJECT_DIR/frontend
Environment=PATH=$NODE_DIR:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
ExecStart=$NPM_BIN start -- --host 0.0.0.0
Restart=on-failure
RestartSec=5
TimeoutStopSec=20

[Install]
WantedBy=default.target
UNIT

cat > "$SYSTEMD_DIR/huellitas-control-panel.service" <<UNIT
[Unit]
Description=Huellitas local service control panel
After=network.target

[Service]
Type=simple
WorkingDirectory=$SCRIPT_DIR
EnvironmentFile=$PANEL_ENV
ExecStart=/usr/bin/python3 $SCRIPT_DIR/server.py
Restart=on-failure
RestartSec=3
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=read-only
ReadWritePaths=/run/user/%U

[Install]
WantedBy=default.target
UNIT

systemctl --user daemon-reload
systemctl --user enable --now huellitas-control-panel.service

echo
echo "Panel instalado y encendido en el puerto 8765."
echo "Token de acceso: $PANEL_ENV"
echo "Abre http://<IP-LOCAL-DEL-MINISERVER>:8765 y pega el token guardado en ese archivo."
echo "Consulta la IP del miniserver con: hostname -I"
echo
echo "Para iniciar el panel automáticamente tras reiniciar el servidor sin iniciar sesión por SSH:"
echo "  sudo loginctl enable-linger $USER"
echo
echo "Si UFW bloquea el acceso desde tu LAN, permite solo tu subred local, por ejemplo:"
echo "  sudo ufw allow from 192.168.8.0/24 to any port 8765 proto tcp"
echo "No agregues el puerto 8765 a Cloudflare Tunnel."
