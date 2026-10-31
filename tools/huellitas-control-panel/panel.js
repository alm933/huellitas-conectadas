    const tokenInput = document.querySelector('#token');
    const login = document.querySelector('#login');
    const controls = document.querySelector('#controls');
    const notice = document.querySelector('#notice');
    let token = sessionStorage.getItem('huellitas-panel-token') || '';
    let busy = false;

    function setNotice(message, error = false) {
      notice.textContent = message;
      notice.classList.toggle('error', error);
    }

    async function request(path, options = {}) {
      const response = await fetch(path, {
        ...options,
        headers: { Authorization: `Bearer ${token}`, ...(options.headers || {}) }
      });
      const data = await response.json();
      if (response.status === 401) {
        token = '';
        sessionStorage.removeItem('huellitas-panel-token');
        login.classList.remove('hidden');
        controls.classList.add('hidden');
      }
      if (!response.ok) throw new Error(data.error || 'No se pudo completar la acción.');
      return data;
    }

    function paintService(name, service) {
      const status = document.querySelector(`#${name}-status`);
      const label = status.querySelector('span');
      const button = document.querySelector(`#${name}-toggle`);
      const on = service.state === 'active' || service.state === 'activating' || service.state === 'reloading';
      const external = service.state === 'external';
      const starting = service.state === 'activating' || service.state === 'reloading';
      status.className = `status ${external ? 'external' : starting ? 'busy' : on ? 'on' : ''}`;
      label.textContent = external ? 'Activo fuera del panel' : starting ? 'Iniciando' : on ? 'Encendido' : service.state === 'failed' ? 'Error' : 'Apagado';
      button.textContent = external ? 'Detén el proceso anterior' : on ? `Apagar ${name === 'spring' ? 'Spring' : 'Angular'}` : `Encender ${name === 'spring' ? 'Spring' : 'Angular'}`;
      button.classList.toggle('off', on);
      button.disabled = busy || external || starting;
      button.dataset.action = on ? 'stop' : 'start';
    }

    async function refresh() {
      if (!token) return;
      try {
        const status = await request('/api/status');
        login.classList.add('hidden');
        controls.classList.remove('hidden');
        paintService('spring', status.spring);
        paintService('angular', status.angular);
      } catch (error) {
        setNotice(error.message, true);
      }
    }

    async function connect() {
      token = tokenInput.value.trim();
      if (!token) { setNotice('Ingresa el token del panel.', true); return; }
      sessionStorage.setItem('huellitas-panel-token', token);
      await refresh();
    }

    async function toggle(name) {
      if (busy) return;
      const button = document.querySelector(`#${name}-toggle`);
      const action = button.dataset.action;
      busy = true;
      document.querySelectorAll('.toggle').forEach(item => item.disabled = true);
      setNotice(`${action === 'start' ? 'Encendiendo' : 'Apagando'} ${name === 'spring' ? 'Spring' : 'Angular'}…`);
      try {
        await request(`/api/services/${name}/${action}`, { method: 'POST' });
        setNotice('Acción enviada a systemd. Actualizando estado…');
        await new Promise(resolve => setTimeout(resolve, 1200));
        await refresh();
      } catch (error) {
        setNotice(error.message, true);
      } finally {
        busy = false;
        await refresh();
      }
    }

    document.querySelector('#connect').addEventListener('click', connect);
    tokenInput.addEventListener('keydown', event => { if (event.key === 'Enter') connect(); });
    document.querySelector('#spring-toggle').addEventListener('click', () => toggle('spring'));
    document.querySelector('#angular-toggle').addEventListener('click', () => toggle('angular'));
    if (token) refresh();
    setInterval(refresh, 4000);
