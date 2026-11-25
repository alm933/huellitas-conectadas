import { apiBaseUrlForHost } from './api-url';

describe('apiBaseUrlForHost', () => {
  it('uses the Huellitas Online API for the apex domain', () => {
    expect(apiBaseUrlForHost('huellitasconectas.online'))
      .toBe('https://api.huellitasconectas.online/api/v1');
  });

  it('uses the Huellitas Online API for the www domain', () => {
    expect(apiBaseUrlForHost('www.huellitasconectas.online'))
      .toBe('https://api.huellitasconectas.online/api/v1');
  });

  it('keeps the existing API for hc.itechk.us', () => {
    expect(apiBaseUrlForHost('hc.itechk.us'))
      .toBe('https://spring.itechk.us/api/v1');
  });

  it('keeps the existing API for local development', () => {
    expect(apiBaseUrlForHost('localhost'))
      .toBe('https://spring.itechk.us/api/v1');
  });
});
