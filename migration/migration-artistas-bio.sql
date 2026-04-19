-- Adicionar coluna bio na tabela artistas (SQL Server)
IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'artistas' AND COLUMN_NAME = 'bio'
)
ALTER TABLE artistas ADD bio NVARCHAR(500) NULL;
