"""Cria credenciais locais sem sobrescrever um ambiente existente."""
from pathlib import Path
import base64
import secrets

root = Path(__file__).resolve().parents[1]
destination = root / '.env'
if destination.exists():
    print('.env já existe; configuração preservada.')
else:
    values = {
        'POSTGRES_DB': 'oficina',
        'POSTGRES_USER': 'postgres',
        'POSTGRES_PASSWORD': secrets.token_urlsafe(32),
        'ADMIN_EMAIL': 'admin@oficina.com',
        'ADMIN_PASSWORD': secrets.token_urlsafe(24),
        'JWT_SECRET': base64.b64encode(secrets.token_bytes(32)).decode(),
        'INTEGRATION_TOKEN': secrets.token_urlsafe(32),
        'JPA_DDL_AUTO': 'validate',
    }
    with destination.open('x', encoding='utf-8') as output:
        output.write(''.join(f'{key}={value}\n' for key, value in values.items()))
    print('Credenciais gravadas em .env (arquivo ignorado pelo Git).')
