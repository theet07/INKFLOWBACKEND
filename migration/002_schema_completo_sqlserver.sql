-- ============================
-- INKFLOW - Schema Completo SQL Server
-- ============================
-- Inclui: Todas as tabelas + Índices de Performance

USE INKFLOW;
GO

-- ============================
-- TABELAS PRINCIPAIS
-- ============================

-- Tabela: cicatrizacoes
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'cicatrizacoes')
CREATE TABLE cicatrizacoes (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    agendamento_id BIGINT NOT NULL UNIQUE,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    periodo_total_dias INT NOT NULL,
    status NVARCHAR(50) NOT NULL DEFAULT 'ATIVA',
    xp_total INT NOT NULL DEFAULT 0,
    dia_atual INT NOT NULL DEFAULT 1,
    fase_atual NVARCHAR(50) NOT NULL DEFAULT 'FASE_1_PRIMEIRAS_24H',
    FOREIGN KEY (agendamento_id) REFERENCES agendamentos(id)
);
GO

-- Tabela: checkpoint_dias
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'checkpoint_dias')
CREATE TABLE checkpoint_dias (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    cicatrizacao_id BIGINT NOT NULL,
    numero_dia INT NOT NULL,
    fase NVARCHAR(50) NOT NULL,
    status_dia NVARCHAR(50) NOT NULL DEFAULT 'BLOQUEADO',
    xp_ganho INT NOT NULL DEFAULT 0,
    estrelas INT NOT NULL DEFAULT 0,
    tem_quiz BIT NOT NULL DEFAULT 0,
    data DATE NOT NULL,
    FOREIGN KEY (cicatrizacao_id) REFERENCES cicatrizacoes(id)
);
GO

-- Tabela: checklist_itens
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'checklist_itens')
CREATE TABLE checklist_itens (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    checkpoint_dia_id BIGINT NOT NULL,
    periodo NVARCHAR(20) NOT NULL,
    ordem INT NOT NULL,
    descricao NVARCHAR(255) NOT NULL,
    concluido BIT NOT NULL DEFAULT 0,
    data_marcacao DATETIME2,
    FOREIGN KEY (checkpoint_dia_id) REFERENCES checkpoint_dias(id)
);
GO

-- Tabela: badges
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'badges')
CREATE TABLE badges (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    nome NVARCHAR(100) NOT NULL,
    descricao NVARCHAR(255) NOT NULL,
    icone NVARCHAR(100) NOT NULL,
    categoria NVARCHAR(50) NOT NULL,
    criterio_tipo NVARCHAR(50) NOT NULL,
    criterio_valor INT NOT NULL
);
GO

-- Tabela: badge_usuario
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'badge_usuario')
CREATE TABLE badge_usuario (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    badge_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,
    desbloqueado BIT NOT NULL DEFAULT 0,
    data_desbloqueio DATETIME2,
    progresso INT NOT NULL DEFAULT 0,
    FOREIGN KEY (badge_id) REFERENCES badges(id),
    FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT UQ_badge_usuario UNIQUE (badge_id, cliente_id)
);
GO

-- Tabela: fotos_evolucao
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'fotos_evolucao')
CREATE TABLE fotos_evolucao (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    cicatrizacao_id BIGINT NOT NULL,
    url_imagem NVARCHAR(500) NOT NULL,
    numero_dia INT NOT NULL,
    data_upload DATETIME2 NOT NULL DEFAULT GETDATE(),
    legenda NVARCHAR(255),
    FOREIGN KEY (cicatrizacao_id) REFERENCES cicatrizacoes(id)
);
GO

-- Tabela: notificacao_preferencias
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'notificacao_preferencias')
CREATE TABLE notificacao_preferencias (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    cliente_id BIGINT NOT NULL UNIQUE,
    horario_manha NVARCHAR(10) NOT NULL DEFAULT '08:00',
    horario_tarde NVARCHAR(10) NOT NULL DEFAULT '14:00',
    horario_noite NVARCHAR(10) NOT NULL DEFAULT '21:00',
    notificacoes_ativas BIT NOT NULL DEFAULT 1,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);
GO

-- Tabela: dicas
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'dicas')
CREATE TABLE dicas (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    titulo NVARCHAR(100) NOT NULL,
    descricao NVARCHAR(500) NOT NULL,
    icone NVARCHAR(100) NOT NULL,
    dia_inicio INT NOT NULL,
    dia_fim INT NOT NULL
);
GO

-- Tabela: quiz_perguntas
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'quiz_perguntas')
CREATE TABLE quiz_perguntas (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    checkpoint_dia_numero INT NOT NULL,
    pergunta NVARCHAR(500) NOT NULL,
    explicacao NVARCHAR(500) NOT NULL,
    resposta_correta INT NOT NULL,
    xp_bonus INT NOT NULL DEFAULT 15
);
GO

-- Tabela: quiz_opcoes
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'quiz_opcoes')
CREATE TABLE quiz_opcoes (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    pergunta_id BIGINT NOT NULL,
    indice INT NOT NULL,
    texto NVARCHAR(255) NOT NULL,
    FOREIGN KEY (pergunta_id) REFERENCES quiz_perguntas(id)
);
GO

-- ============================
-- ÍNDICES DE PERFORMANCE
-- ============================

-- Índice 1: Cicatrização por agendamento e status
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_cicatrizacao_agendamento_status')
CREATE NONCLUSTERED INDEX idx_cicatrizacao_agendamento_status 
ON cicatrizacoes(agendamento_id, status);
GO

-- Índice 2: Checkpoint por cicatrização e número do dia
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_checkpoint_cicatrizacao_dia')
CREATE NONCLUSTERED INDEX idx_checkpoint_cicatrizacao_dia 
ON checkpoint_dias(cicatrizacao_id, numero_dia);
GO

-- Índice 3: Checklist items por checkpoint
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_checklist_checkpoint')
CREATE NONCLUSTERED INDEX idx_checklist_checkpoint 
ON checklist_itens(checkpoint_dia_id);
GO

-- Índice 4: Badges por usuário
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_badge_usuario_cliente')
CREATE NONCLUSTERED INDEX idx_badge_usuario_cliente 
ON badge_usuario(cliente_id);
GO

-- Índice 5: Fotos por cicatrização
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_foto_cicatrizacao')
CREATE NONCLUSTERED INDEX idx_foto_cicatrizacao 
ON fotos_evolucao(cicatrizacao_id);
GO

-- Índice 6: Notificações por usuário
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_notificacao_cliente')
CREATE NONCLUSTERED INDEX idx_notificacao_cliente 
ON notificacao_preferencias(cliente_id);
GO

PRINT 'Schema completo criado com sucesso!';
PRINT 'Tabelas: 10 | Índices: 6';
GO
