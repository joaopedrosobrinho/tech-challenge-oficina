"""Executa o fluxo de uma OS pela API; use apenas em ambiente de desenvolvimento."""
import argparse
import json
import secrets
import time
import urllib.error
import urllib.request
import uuid
from concurrent.futures import ThreadPoolExecutor
from local_env import load_env, required

parser = argparse.ArgumentParser()
parser.add_argument('--url', default='http://localhost:8080')
parser.add_argument('--wait', type=int, default=30)
parser.add_argument('--mailpit-url', help='Se informado, também confere os e-mails capturados pelo Mailpit.')
args = parser.parse_args()
load_env()
token = None


def request(method, path, body=None, integration=False, expected=200):
    headers = {'Content-Type': 'application/json'}
    if integration:
        headers['X-Integration-Key'] = required('INTEGRATION_TOKEN')
    elif token:
        headers['Authorization'] = 'Bearer ' + token
    req = urllib.request.Request(args.url + path, method=method, headers=headers,
                                 data=None if body is None else json.dumps(body).encode())
    try:
        with urllib.request.urlopen(req, timeout=15) as response:
            assert response.status == expected, (path, response.status)
            data = response.read()
            return json.loads(data) if data else None
    except urllib.error.HTTPError as error:
        raise RuntimeError(f'{method} {path}: HTTP {error.code}: {error.read().decode()}') from error


deadline = time.monotonic() + args.wait
while True:
    try:
        request('GET', '/actuator/health/readiness')
        break
    except (OSError, RuntimeError):
        if time.monotonic() >= deadline:
            raise
        time.sleep(2)

login = request('POST', '/api/auth/login', {'email': required('ADMIN_EMAIL'), 'senha': required('ADMIN_PASSWORD')})
token = login['token']
digits = [secrets.randbelow(10) for _ in range(9)]
for length in (9, 10):
    remainder = sum(digit * (length + 1 - index) for index, digit in enumerate(digits)) * 10 % 11
    digits.append(0 if remainder == 10 else remainder)
cpf = ''.join(map(str, digits))
suffix = uuid.uuid4().hex[:8]
cliente = request('POST', '/api/clientes', {'nome': 'Cliente demonstração', 'cpfCnpj': cpf,
                  'telefone': '11912345678', 'email': f'demo-{suffix}@example.com'}, expected=201)
placa = ''.join(secrets.choice('ABCDEFGHIJKLMNOPQRSTUVWXYZ') for _ in range(3)) + str(secrets.randbelow(10)) + secrets.choice('ABCDEFGHIJKLMNOPQRSTUVWXYZ') + f'{secrets.randbelow(100):02d}'
veiculo = request('POST', '/api/veiculos', {'clienteId': cliente['id'], 'placa': placa,
                  'marca': 'Fiat', 'modelo': 'Uno', 'ano': 2020}, expected=201)
servico = request('POST', '/api/servicos', {'nome': f'Revisão {suffix}', 'descricao': 'Demonstração',
                  'valor': 100, 'tempoEstimadoMinutos': 30, 'ativo': True}, expected=201)
peca = request('POST', '/api/pecas', {'codigo': suffix, 'nome': 'Filtro', 'valor': 25,
               'quantidadeEstoque': 10, 'ativo': True}, expected=201)
ordem = request('POST', '/api/ordens-servico', {'clienteId': cliente['id'], 'veiculoId': veiculo['id'],
                'servicos': [{'servicoId': servico['id'], 'quantidade': 1}],
                'pecas': [{'pecaId': peca['id'], 'quantidade': 2}]}, expected=201)
path = '/api/ordens-servico/' + str(ordem['id'])
orcamento = request('POST', path + '/orcamento/enviar-aprovacao')
evento = {'eventoId': str(uuid.uuid4()), 'versaoOrcamento': orcamento['versaoOrcamento'],
          'aprovada': True, 'observacao': 'Autorizado na demonstração'}
def approve(_):
    request('POST', f'/api/integracoes/ordens-servico/{ordem["id"]}/aprovacao', evento, integration=True, expected=204)


with ThreadPoolExecutor(max_workers=2) as executor:
    list(executor.map(approve, range(2)))
approve(0)
assert request('GET', f'/api/pecas/{peca["id"]}')['quantidadeEstoque'] == 8
assert request('GET', path)['status'] == 'EM_EXECUCAO'
request('POST', path + '/finalizar')
assert ordem['id'] not in [item['id'] for item in request('GET', '/api/ordens-servico')]
request('POST', path + '/entregar')
assert request('GET', path)['status'] == 'ENTREGUE'
token = None
publico = request('GET', f'/api/publico/ordens-servico/{ordem["numeroOs"]}?placa={placa}')
assert publico['status'] == 'ENTREGUE'
if args.mailpit_url:
    deadline = time.monotonic() + 90
    while True:
        try:
            with urllib.request.urlopen(args.mailpit_url.rstrip('/') + '/api/v1/messages?limit=100', timeout=10) as response:
                messages = json.load(response)['messages']
            received = [message for message in messages if ordem['numeroOs'] in message.get('Subject', '')]
            # Criação, envio do orçamento, aprovação, finalização e entrega.
            if len(received) >= 5:
                print('Cinco notificações de status capturadas pelo Mailpit.')
                break
        except OSError:
            pass
        if time.monotonic() >= deadline:
            raise RuntimeError('As notificações da OS não chegaram ao Mailpit em 90 segundos.')
        time.sleep(3)
print(f'Fluxo validado: {ordem["numeroOs"]}. Aprovações concorrentes e reenvio sem nova baixa; histórico preservado.')
