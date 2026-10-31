# Panel local para controlar Huellitas

Esta página permite encender y apagar Spring Boot y Angular desde un navegador de la red local. Los servicios se ejecutan como unidades de `systemd --user`; el panel no acepta comandos arbitrarios.

## Requisitos

- Ubuntu Server con `systemd` y Python 3.
- El repositorio en `~/proyects/test/demoone`.
- Node.js y npm disponibles en el `PATH` del usuario `ale`.
- Configuración de Spring en `~/.config/huellitas/garage.env`.

## Instalación única

Desde el repositorio, con el usuario `ale`:

```bash
cd /home/ale/proyects/test/demoone/tools/huellitas-control-panel
chmod +x install.sh
./install.sh
```

El instalador configura tres unidades de usuario y arranca únicamente el panel. No inicia Spring ni Angular hasta que pulses sus botones. Para ver el token:

```bash
cat ~/.config/huellitas/control-panel.env
```

Consulta la IP del miniserver con `hostname -I` y abre `http://IP-LOCAL:8765` desde un equipo de la misma red. La primera vez, pega el token `HUELLITAS_PANEL_TOKEN` en la página. Se guarda solo mientras la pestaña siga abierta.

Si tienes UFW activo, permite el puerto solo desde tu subred local, por ejemplo:

```bash
sudo ufw allow from 192.168.8.0/24 to any port 8765 proto tcp
```

Para que el panel vuelva a estar disponible después de reiniciar el miniserver, habilita una vez el servicio de usuario persistente:

```bash
sudo loginctl enable-linger ale
```

## Primer uso y conflictos de puertos

Si Spring o Angular ya están ejecutándose desde una terminal, el panel los marcará como **Activo fuera del panel** en los puertos 8080 o 4200. Detén esos procesos una sola vez con `Ctrl+C` en sus terminales; luego podrás controlarlos desde esta página.

Spring usa el mismo archivo de configuración que el comando actual, `~/.config/huellitas/garage.env`. Angular se inicia con `npm start -- --host 0.0.0.0`, en el puerto 4200.

## Seguridad y uso

- El panel escucha en la red local en el puerto 8765 y exige un token aleatorio para consultar estado y ejecutar acciones.
- Los botones solo pueden operar `huellitas-spring.service` y `huellitas-angular.service`, con acciones `start`, `stop` y `restart`.
- No reenvíes el puerto 8765 ni lo publiques con Cloudflare Tunnel.
- Si necesitas invalidar el token, detén el panel, reemplaza `HUELLITAS_PANEL_TOKEN` en `~/.config/huellitas/control-panel.env` por un valor aleatorio largo y reinicia `huellitas-control-panel.service`.

## Diagnóstico

```bash
systemctl --user status huellitas-control-panel.service
journalctl --user -u huellitas-control-panel.service -n 50 --no-pager
journalctl --user -u huellitas-spring.service -n 80 --no-pager
journalctl --user -u huellitas-angular.service -n 80 --no-pager
```
