"""Publica no cluster Kind provisionado pelos módulos Terraform."""
import argparse
import base64
import json
import subprocess
from local_env import ROOT, load_env, required

parser = argparse.ArgumentParser()
parser.add_argument('--image', default='tech-challenge-oficina:local')
parser.add_argument('--cluster', default='oficina-fase2')
args = parser.parse_args()
load_env()
config = ROOT / 'infra/cluster/kubeconfig'
kubectl = ['kubectl', '--kubeconfig', str(config)]


def run(command, **kwargs):
    subprocess.run(command, cwd=ROOT, check=True, **kwargs)


values = {key: required(key) for key in ['ADMIN_EMAIL', 'ADMIN_PASSWORD', 'JWT_SECRET', 'INTEGRATION_TOKEN']}
if len(values['INTEGRATION_TOKEN']) < 32 or len(base64.b64decode(values['JWT_SECRET'], validate=True)) < 32:
    raise SystemExit('INTEGRATION_TOKEN e JWT_SECRET devem ter pelo menos 32 caracteres/bytes, respectivamente.')
secret = {'apiVersion': 'v1', 'kind': 'Secret', 'metadata': {'name': 'oficina-secret', 'namespace': 'oficina'},
          'type': 'Opaque', 'stringData': values}
run(kubectl + ['apply', '-f', '-'], input=json.dumps(secret), text=True, stdout=subprocess.DEVNULL)
run(kubectl + ['apply', '-f', 'k8s/local/mailpit.yaml'])
run(kubectl + ['apply', '-f', 'https://github.com/kubernetes-sigs/metrics-server/releases/download/v0.8.0/components.yaml'])
# Kind usa certificados de kubelet locais; esta exceção não pertence ao perfil cloud.
patch = [{'op': 'add', 'path': '/spec/template/spec/containers/0/args/-', 'value': '--kubelet-insecure-tls'}]
run(kubectl + ['-n', 'kube-system', 'patch', 'deployment', 'metrics-server', '--type=json', '-p', json.dumps(patch)])
run(kubectl + ['-n', 'kube-system', 'rollout', 'status', 'deployment/metrics-server', '--timeout=180s'])
run(['kind', 'load', 'docker-image', args.image, '--name', args.cluster])
overlay = ROOT / 'target/deploy'
overlay.mkdir(parents=True, exist_ok=True)
(overlay / 'kustomization.yaml').write_text(json.dumps({
    'apiVersion': 'kustomize.config.k8s.io/v1beta1', 'kind': 'Kustomization',
    'resources': ['../../k8s/app'],
    'images': [{'name': 'tech-challenge-oficina', 'newName': args.image.rsplit(':', 1)[0],
                'newTag': args.image.rsplit(':', 1)[1]}],
}), encoding='utf-8')
run(kubectl + ['-n', 'oficina', 'rollout', 'status', 'deployment/postgres', '--timeout=180s'])
run(kubectl + ['apply', '-k', str(overlay)])
run(kubectl + ['-n', 'oficina', 'rollout', 'status', 'deployment/oficina', '--timeout=300s'])
run(kubectl + ['-n', 'oficina', 'get', 'pods,svc,hpa'])
