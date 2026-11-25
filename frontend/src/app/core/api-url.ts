const ONLINE_FRONTEND_HOSTS = new Set([
  'huellitasconectas.online',
  'www.huellitasconectas.online'
]);

const API_ORIGIN_BY_FRONTEND_HOST: Record<string, string> = {
  'huellitasconectas.online': 'https://api.huellitasconectas.online',
  'www.huellitasconectas.online': 'https://api.huellitasconectas.online'
};

export function apiBaseUrlForHost(hostname: string): string {
  const normalizedHost = hostname.toLowerCase();
  const apiOrigin = ONLINE_FRONTEND_HOSTS.has(normalizedHost)
    ? API_ORIGIN_BY_FRONTEND_HOST[normalizedHost]
    : 'https://spring.itechk.us';

  return `${apiOrigin}/api/v1`;
}

export const API_BASE_URL = apiBaseUrlForHost(window.location.hostname);
