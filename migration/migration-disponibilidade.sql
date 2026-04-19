-- Criar tabela disponibilidade_artistas (SQL Server)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='disponibilidade_artistas' AND xtype='U')
CREATE TABLE disponibilidade_artistas (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    artista_id INT NOT NULL,
    dia_semana INT NOT NULL,       -- 0=Seg, 1=Ter, 2=Qua, 3=Qui, 4=Sex, 5=Sab, 6=Dom
    hora_inicio NVARCHAR(5) NOT NULL,  -- HH:mm
    hora_fim NVARCHAR(5) NOT NULL,     -- HH:mm
    duracao_slot_minutos INT NOT NULL DEFAULT 60,
    ativo BIT NOT NULL DEFAULT 1,
    CONSTRAINT FK_disp_artista FOREIGN KEY (artista_id) REFERENCES artistas(id),
    CONSTRAINT UQ_disp_artista_dia UNIQUE (artista_id, dia_semana)
);

-- Disponibilidade padrao: todos os artistas trabalham Seg-Sex das 09:00 as 18:00
INSERT INTO disponibilidade_artistas (artista_id, dia_semana, hora_inicio, hora_fim, duracao_slot_minutos)
SELECT id, dia, '09:00', '18:00', 60
FROM artistas
CROSS JOIN (VALUES (0),(1),(2),(3),(4)) AS dias(dia)
WHERE ativo = 1
AND NOT EXISTS (
    SELECT 1 FROM disponibilidade_artistas d
    WHERE d.artista_id = artistas.id AND d.dia_semana = dia
);
