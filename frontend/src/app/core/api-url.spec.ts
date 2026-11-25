import { API_BASE_URL } from './api-url';

describe('API_BASE_URL', () => {
  it('keeps using the existing Spring API for the new frontend domain', () => {
    expect(API_BASE_URL).toBe('https://spring.itechk.us/api/v1');
  });
});
