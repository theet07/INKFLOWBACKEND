-- Script simples sem criar banco (usar banco existente)

-- Criar tabela clientes
CREATE TABLE clientes (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) UNIQUE NOT NULL,
    email NVARCHAR(100) UNIQUE NOT NULL,
    password NVARCHAR(255) NOT NULL,
    full_name NVARCHAR(100),
    bio NTEXT,
    profile_image NVARCHAR(255),
    created_at DATETIME2 DEFAULT GETDATE()
);

-- Criar tabela agendamentos
CREATE TABLE agendamentos (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    data_hora DATETIME2 NOT NULL,
    servico NVARCHAR(100),
    descricao NTEXT,
    status NVARCHAR(20) DEFAULT 'AGENDADO',
    preco DECIMAL(10,2),
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

-- Inserir dados de teste
INSERT INTO clientes (username, email, password, full_name) VALUES
('joao123', 'joao@email.com', '123456', 'João Silva'),
('maria456', 'maria@email.com', '123456', 'Maria Santos'),
('pedro789', 'pedro@email.com', '123456', 'Pedro Costa');

INSERT INTO agendamentos (cliente_id, data_hora, servico, descricao, preco) VALUES
(1, '2024-01-15 14:00:00', 'Tatuagem Tradicional', 'Rosa no braço direito', 250.00),
(2, '2024-01-16 10:00:00', 'Tatuagem Fine Line', 'Borboleta no pulso', 180.00),
(3, '2024-01-17 16:00:00', 'Tatuagem Blackwork', 'Mandala nas costas', 400.00);