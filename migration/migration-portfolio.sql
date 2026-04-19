-- Criar tabela portfolio_items (SQL Server)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='portfolio_items' AND xtype='U')
CREATE TABLE portfolio_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    imagem_url NVARCHAR(1000) NOT NULL,
    categoria NVARCHAR(255) NULL,
    descricao NVARCHAR(500) NULL,
    artista_id INT NOT NULL,
    CONSTRAINT FK_portfolio_artista FOREIGN KEY (artista_id) REFERENCES artistas(id)
);
