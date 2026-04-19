-- Criar tabela de admins
CREATE TABLE IF NOT EXISTS admins (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Inserir o primeiro admin manualmente com seu email e hash BCrypt
-- Gere o hash com: https://bcrypt-generator.com (rounds: 12)
-- INSERT INTO admins (nome, email, password)
-- VALUES ('Seu Nome', 'seu@email.com', '$2a$12$SEU_HASH_AQUI')
-- ON CONFLICT (email) DO NOTHING;
