"""Gera carga no cluster local e registra a resposta do HPA."""
import json
import subprocess
import time
from local_env import ROOT

kubectl = ['kubectl', '--kubeconfig', str(ROOT / 'infra/cluster/kubeconfig'), '-n', 'oficina']


def run(arguments, **kwargs):
    return subprocess.run(kubectl + arguments, check=True, cwd=ROOT, text=True, **kwargs)


script = (ROOT / 'scripts/load-test.js').read_text(encoding='utf-8')
config = {'apiVersion': 'v1', 'kind': 'ConfigMap', 'metadata': {'name': 'oficina-load-script', 'namespace': 'oficina'},
          'data': {'load-test.js': script}}
run(['apply', '-f', '-'], input=json.dumps(config))
run(['delete', 'job', 'oficina-load-test', '--ignore-not-found=true'])
run(['apply', '-f', 'k8s/local/load-test.yaml'])
samples = []
deadline = time.monotonic() + 330
scaled = False
while time.monotonic() < deadline:
    hpa = json.loads(run(['get', 'hpa', 'oficina', '-o', 'json'], capture_output=True).stdout)
    status = hpa.get('status', {})
    sample = {'time': time.strftime('%Y-%m-%dT%H:%M:%S%z'), **status}
    samples.append(sample)
    replicas = status.get('currentReplicas', 0)
    valid_metrics = any(c['type'] == 'ScalingActive' and c['status'] == 'True' for c in status.get('conditions', []))
    scaled |= replicas > hpa['spec']['minReplicas'] and valid_metrics
    print(f'Réplicas atuais: {replicas}; desejadas: {status.get("desiredReplicas", 0)}; métricas disponíveis: {valid_metrics}', flush=True)
    job = json.loads(run(['get', 'job', 'oficina-load-test', '-o', 'json'], capture_output=True).stdout)
    if job.get('status', {}).get('succeeded') or job.get('status', {}).get('failed'):
        break
    time.sleep(15)
destination = ROOT / 'target/cluster'
destination.mkdir(parents=True, exist_ok=True)
(destination / 'hpa.json').write_text(json.dumps(samples, indent=2), encoding='utf-8')
logs = run(['logs', 'job/oficina-load-test'], capture_output=True).stdout
(destination / 'carga.log').write_text(logs, encoding='utf-8')
if not job.get('status', {}).get('succeeded'):
    raise SystemExit('A carga não terminou com sucesso. Consulte target/cluster/carga.log.')
if not scaled:
    raise SystemExit('O HPA não escalou acima do mínimo. Verifique métricas e capacidade do Docker.')
print('Escala automática observada; evidências salvas em target/cluster.')
