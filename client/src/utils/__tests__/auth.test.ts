import { describe, it, expect, beforeEach } from 'vitest';

// We test pure logic functions only (no DOM-dependent ones)

describe('decodeToken', () => {
  // Create a real base64url-encoded payload
  const payloadObj = { sub: '1', email: 'admin@admin.com', role: 'ADMIN', iat: 1712345678, exp: 9999999999 };
  const header = btoa(JSON.stringify({ alg: 'HS256' }));
  const payload = btoa(JSON.stringify(payloadObj));
  const TOKEN = `${header}.${payload}.signature`;

  it('should decode a valid JWT payload', async () => {
    const { decodeToken } = await import('../auth');
    const result = decodeToken(TOKEN);
    expect(result).not.toBeNull();
    expect(result?.email).toBe('admin@admin.com');
    expect(result?.role).toBe('ADMIN');
  });

  it('should return null for invalid token', async () => {
    const { decodeToken } = await import('../auth');
    expect(decodeToken('invalid')).toBeNull();
    expect(decodeToken('a.b.c')).toBeNull();
    expect(decodeToken('')).toBeNull();
  });
});

describe('getSession', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('should return null when no session exists', async () => {
    const { getSession } = await import('../auth');
    expect(getSession()).toBeNull();
  });

  it('should return session when stored', async () => {
    const session = { token: 'abc', user: { id: 1, email: 'test@test.com', nombre: 'Test', apellido: 'User', celular: null, role: 'USUARIO' as const } };
    localStorage.setItem('foodstore_session', JSON.stringify(session));
    const { getSession } = await import('../auth');
    const result = getSession();
    expect(result).not.toBeNull();
    expect(result?.user.email).toBe('test@test.com');
    expect(result?.user.role).toBe('USUARIO');
  });

  it('should return null on corrupted data', async () => {
    localStorage.setItem('foodstore_session', '{corrupted');
    const { getSession } = await import('../auth');
    expect(getSession()).toBeNull();
  });
});

describe('isAuthenticated', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('should return false when no session', async () => {
    const { isAuthenticated } = await import('../auth');
    expect(isAuthenticated()).toBe(false);
  });

  it('should return false for expired token', async () => {
    // Token that expired in 1970
    const expiredPayload = {
      sub: '1', email: 'test@test.com', role: 'USUARIO',
      exp: 1, iat: 0,
    };
    const expiredToken = btoa('{"alg":"HS256"}') + '.' + btoa(JSON.stringify(expiredPayload)) + '.sig';
    localStorage.setItem('foodstore_session', JSON.stringify({ token: expiredToken, user: {} }));
    const { isAuthenticated } = await import('../auth');
    expect(isAuthenticated()).toBe(false);
  });
});
