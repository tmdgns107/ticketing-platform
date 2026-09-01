// Lock-strategy benchmark: many VUs fight over a SMALL pool of seats using one
// concurrency-control strategy, so you can compare PESSIMISTIC / OPTIMISTIC /
// DISTRIBUTED on throughput, p95 latency and conflict rate.
//
//   k6 run -e STRATEGY=PESSIMISTIC -e SEAT_POOL=5 load/k6/lock-benchmark.js
//   k6 run -e STRATEGY=OPTIMISTIC  load/k6/lock-benchmark.js
//   k6 run -e STRATEGY=DISTRIBUTED load/k6/lock-benchmark.js
//
// Between runs, reset the seats (re-run the dev seed or restart with a fresh DB).

import { check } from 'k6';
import http from 'k6/http';
import { Counter, Trend } from 'k6/metrics';
import { BASE, login, authHeaders, seatMap } from './lib/common.js';

const STRATEGY = __ENV.STRATEGY || 'OPTIMISTIC';
const SCHEDULE_ID = Number(__ENV.SCHEDULE_ID || 1);
const SEAT_POOL = Number(__ENV.SEAT_POOL || 10);

const won = new Counter('hold_won');
const conflict = new Counter('hold_conflict');
const holdLatency = new Trend('hold_latency_ms', true);

export const options = {
  scenarios: {
    rush: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '15s', target: 100 },
        { duration: '30s', target: 100 },
        { duration: '5s', target: 0 },
      ],
    },
  },
  thresholds: {
    hold_won: ['count>0'],
  },
};

export function setup() {
  const token = login(`bench-admin@loadtest.dev`);
  const seats = seatMap(SCHEDULE_ID)
    .seats.filter((s) => s.status === 'AVAILABLE')
    .slice(0, SEAT_POOL)
    .map((s) => s.id);
  return { seatIds: seats, token };
}

export default function (data) {
  const token = login(`bench-${__VU}@loadtest.dev`);
  const seatId = data.seatIds[Math.floor(Math.random() * data.seatIds.length)];

  const res = http.post(
    `${BASE}/api/v1/reservations?lockStrategy=${STRATEGY}`,
    JSON.stringify({ scheduleId: SCHEDULE_ID, seatId }),
    authHeaders(token),
  );
  holdLatency.add(res.timings.duration);

  if (res.status === 201) {
    won.add(1);
  } else if (res.status === 409) {
    conflict.add(1);
  }
  check(res, { 'hold resolved (201/409)': (r) => r.status === 201 || r.status === 409 });
}
