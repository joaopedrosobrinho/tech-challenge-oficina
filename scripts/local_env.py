from pathlib import Path
import os

ROOT = Path(__file__).resolve().parents[1]


def load_env():
    source = ROOT / '.env'
    if source.exists():
        for line in source.read_text(encoding='utf-8-sig').splitlines():
            if line.strip() and not line.lstrip().startswith('#') and '=' in line:
                key, value = line.split('=', 1)
                os.environ.setdefault(key.strip(), value.strip().strip('\"\''))


def required(key):
    value = os.environ.get(key, '')
    if not value:
        raise SystemExit(f'Defina {key} no ambiente ou em .env.')
    return value
