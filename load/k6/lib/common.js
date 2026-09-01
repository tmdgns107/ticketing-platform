import http from 'k6/http';
import { check } from 'k6';

export const BASE = __ENV.BASE_URL || 'http://localhost:8080';

/** Signs up (ignoring "already exists") and logs in, returning the access token. */
export function login(email, password = 'password1', nickname = 'loadtest') {
  http.post(
    `${BASE}/api/v1/auth/signup`,
    JSON.stringify({ email, password, nickname }),
    { headers: { 'Content-Type': 'application/json' } },
  );
  const res = http.post(
    `${BASE}/api/v1/auth/login`,
    JSON.stringify({ email, password }),
    { headers: { 'Content-Type': 'application/json' } },
  );
  check(res, { 'login 200': (r) => r.status === 200 });
  return res.json('data.accessToken');
}

export function authHeaders(token, extra = {}) {
  return { headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}`, ...extra } };
}

export function seatMap(scheduleId) {
  return http.get(`${BASE}/api/v1/schedules/${scheduleId}/seats`).json('data');
}
