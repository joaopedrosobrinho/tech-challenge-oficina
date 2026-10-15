import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '3m', target: 30 },
    { duration: '30s', target: 0 },
  ],
  thresholds: { http_req_failed: ['rate<0.05'] },
};

export default function () {
  const response = http.post('http://oficina:8080/api/auth/login', JSON.stringify({
    email: __ENV.ADMIN_EMAIL,
    senha: __ENV.ADMIN_PASSWORD,
  }), { headers: { 'Content-Type': 'application/json' } });
  check(response, { 'login retorna 200': (r) => r.status === 200 });
  if (response.status === 200) {
    const token = response.json('token');
    const ordens = http.get('http://oficina:8080/api/ordens-servico', {
      headers: { Authorization: `Bearer ${token}` },
    });
    check(ordens, { 'listagem retorna 200': (r) => r.status === 200 });
  }
  sleep(0.2);
}
