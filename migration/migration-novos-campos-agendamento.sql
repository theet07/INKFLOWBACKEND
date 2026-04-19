-- Migration: novos campos na tabela agendamentos
-- Executar no banco INKFLOW (SQL Server)

USE INKFLOW;

ALTER TABLE agendamentos ADD regiao NVARCHAR(100) NULL;
ALTER TABLE agendamentos ADD largura DECIMAL(6,2) NULL;
ALTER TABLE agendamentos ADD altura DECIMAL(6,2) NULL;
ALTER TABLE agendamentos ADD tags NVARCHAR(500) NULL;
ALTER TABLE agendamentos ADD imagem_referencia_url NVARCHAR(1000) NULL;
