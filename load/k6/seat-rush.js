// End-to-end on-sale rush: each VU walks the real funnel —
//   enter waiting queue -> poll until admitted -> grab a seat -> pay.
// Run the API with QUEUE_ENFORCE=true to exercise the admission gate.
//
//   k6 run load/k6/seat-rush.js
//   k6 run -e VUS=500 -e DURATION=2m load/k6/seat-rush.js

import { check, sleep } from 'k6';
import http from 'k6/http';
import { Counter } from 'k6/metrics';
import { BASE, login, authHeaders, seatMap } from './lib/common.js';

const SCHEDULE_ID = Number(__ENV.SCHEDULE_ID || 1);
const purchased = new Counter('purchased');
const soldOut = new Counter('sold_out');

export const options = {
  scenarios: {
    rush: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '20s', target: Number(__ENV.VUS || 200) },
        { duration: __ENV.DURATION || '1m', target: Number(__ENV.VUS || 200) },
        { duration: '10s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.30'],
    http_req_duration: ['p(95)<1500'],
  },
};

export function setup() {
  const seatIds = seatMap(SCHEDULE_ID).seats.map((s) => s.id);
  return { seatIds };
}

export default function (data) {
  const token = login(`rush-${__VU}-${__ITER}@loadtest.dev`);
  const auth = authHeaders(token);

  http.post(`${BASE}/api/v1/waiting-queue/${SCHEDULE_ID}/enter`, null, auth);

  let entryToken = null;
  for (let i = 0; i < 20 && !entryToken; i++) {
    sleep(0.5);
    const status = http.get(`${BASE}/api/v1/waiting-queue/${SCHEDULE_ID}/status`, auth).json('data');
    if (status && status.admitted) entryToken = status.entryToken;
  }
  if (!entryToken) return;

  const reserveAuth = authHeaders(token, { 'X-Queue-Token': entryToken });
  for (let attempt = 0; attempt < 5; attempt++) {
    const seatId = data.seatIds[Math.floor(Math.random() * data.seatIds.length)];
    const held = http.post(
      `${BASE}/api/v1/reservations`,
      JSON.stringify({ scheduleId: SCHEDULE_ID, seatId }),
      reserveAuth,
    );
    if (held.status === 201) {
      const reservationId = held.json('data.id');
      const pay = http.post(
        `${BASE}/api/v1/payments`,
        JSON.stringify({ reservationId }),
        authHeaders(token, { 'Idempotency-Key': `k6-${__VU}-${__ITER}-${attempt}` }),
      );
      check(pay, { 'paid': (r) => r.status === 201 });
      if (pay.status === 201) purchased.add(1);
      return;
    }
    if (held.status === 409) soldOut.add(1);
  }
}
