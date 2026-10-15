ALTER TABLE ordens_servico ADD COLUMN versao BIGINT NOT NULL DEFAULT 0;
ALTER TABLE ordens_servico ADD COLUMN versao_orcamento BIGINT NOT NULL DEFAULT 0;
CREATE INDEX IF NOT EXISTS idx_os_fila ON ordens_servico(status, data_criacao, id);
