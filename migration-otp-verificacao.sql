-- Migration: adicionar campos de verificacao de e-mail na tabela clientes (SQL Server)
IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'clientes' AND COLUMN_NAME = 'codigo_verificacao'
)
ALTER TABLE clientes ADD codigo_verificacao NVARCHAR(6) NULL;

IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'clientes' AND COLUMN_NAME = 'conta_verificada'
)
ALTER TABLE clientes ADD conta_verificada BIT NOT NULL DEFAULT 1;

-- Clientes existentes ja sao considerados verificados
UPDATE clientes SET conta_verificada = 1 WHERE conta_verificada IS NULL;
