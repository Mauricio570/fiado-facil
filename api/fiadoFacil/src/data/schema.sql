-- ============================================================
-- Fiado Fácil — script completo (schema + dados de exemplo)
-- Rode esse arquivo inteiro pra recriar o banco do zero.
-- ============================================================

DROP TABLE IF EXISTS parcela CASCADE;
DROP TABLE IF EXISTS pagamento CASCADE;
DROP TABLE IF EXISTS item_venda CASCADE;
DROP TABLE IF EXISTS venda CASCADE;
DROP TABLE IF EXISTS cliente CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;

CREATE TABLE usuario (
    id              BIGSERIAL PRIMARY KEY,
    nome_empresa    VARCHAR(150) NOT NULL,
    cnpj            VARCHAR(18)  NOT NULL UNIQUE,
    email           VARCHAR(150) NOT NULL UNIQUE,
    senha           VARCHAR(255) NOT NULL,
    data_criacao    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cliente (
    id              BIGSERIAL PRIMARY KEY,
    fk_usuario      BIGINT       NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    nome            VARCHAR(150) NOT NULL,
    telefone        VARCHAR(20),
    cpf             VARCHAR(14),
    endereco        VARCHAR(255),
    data_criacao    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Impede cliente duplicado (mesmo CPF ou telefone) por usuário/
    -- estabelecimento. NULL continua permitido múltiplas vezes
    -- (CPF/telefone são opcionais) — só bloqueia valor repetido.
    CONSTRAINT uq_cliente_usuario_cpf UNIQUE (fk_usuario, cpf),
    CONSTRAINT uq_cliente_usuario_telefone UNIQUE (fk_usuario, telefone)
);

CREATE TABLE venda (
    id              BIGSERIAL PRIMARY KEY,
    fk_cliente      BIGINT      NOT NULL REFERENCES cliente(id) ON DELETE RESTRICT,
    status          VARCHAR(20) NOT NULL DEFAULT 'EM_ABERTO',
    data_criacao    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE item_venda (
    id              BIGSERIAL PRIMARY KEY,
    fk_venda        BIGINT        NOT NULL REFERENCES venda(id) ON DELETE CASCADE,
    nome            VARCHAR(150)  NOT NULL,
    quantidade      INTEGER       NOT NULL DEFAULT 1,
    valor_unitario  NUMERIC(10,2) NOT NULL,
    valor_total     NUMERIC(10,2) NOT NULL
);

CREATE TABLE pagamento (
    id                     BIGSERIAL PRIMARY KEY,
    fk_venda               BIGINT      NOT NULL UNIQUE REFERENCES venda(id) ON DELETE CASCADE,
    forma_pagamento        VARCHAR(20) NOT NULL,
    quantidade_parcelas    INTEGER     NOT NULL DEFAULT 1,
    juros_mes              NUMERIC(5,2)  NOT NULL DEFAULT 0,
    valor_entrada          NUMERIC(10,2) NOT NULL DEFAULT 0
);

CREATE TABLE parcela (
    id              BIGSERIAL PRIMARY KEY,
    fk_pagamento    BIGINT        NOT NULL REFERENCES pagamento(id) ON DELETE CASCADE,
    numero          INTEGER       NOT NULL,
    valor           NUMERIC(10,2) NOT NULL,
    data_pagamento  DATE,
    status          VARCHAR(20)   NOT NULL DEFAULT 'EM_ABERTO'
);

-- ============================================================
-- Índices para as buscas mais comuns do sistema (por e-mail no
-- login, por cliente nas telas de painel/histórico)
-- ============================================================
CREATE INDEX idx_cliente_fk_usuario ON cliente(fk_usuario);
CREATE INDEX idx_venda_fk_cliente ON venda(fk_cliente);
CREATE INDEX idx_item_venda_fk_venda ON item_venda(fk_venda);
CREATE INDEX idx_parcela_fk_pagamento ON parcela(fk_pagamento);

-- ============================================================
-- INSERTS (dados de exemplo)
-- ============================================================

-- USUARIO
-- senha em texto puro: 'senha123'
INSERT INTO usuario (nome_empresa, cnpj, email, senha) VALUES
('Mercearia Bom Preço', '12.345.678/0001-90', 'teste@gmail.com',
 '$2b$12$8e/fhDONKWTwNtVg5NtMYezl9oZp.tWdoPtna5Vn4K2eC4.bWwM1a');

-- senha em texto puro: 'senha456'
INSERT INTO usuario (nome_empresa, cnpj, email, senha) VALUES
('Padaria Pão Dourado', '98.765.432/0001-10', 'teste2@gmail.com',
 '$2b$12$2CjpByidNAmQCsBpaOFToOJhujwbu01uQyV/3/Win331Yt/cQfcGu');

-- CLIENTE (ambos cadastrados pelo usuário 1 — mesma mercearia)
INSERT INTO cliente (fk_usuario, nome, telefone, cpf, endereco) VALUES
(1, 'Maria Silva', '(51) 99999-1111', '123.456.789-00', 'Rua das Flores, 123 - Porto Alegre/RS');

INSERT INTO cliente (fk_usuario, nome, telefone, cpf, endereco) VALUES
(1, 'João Pedro', '(51) 98888-2222', '987.654.321-00', 'Av. Brasil, 456 - Porto Alegre/RS');

-- VENDA
INSERT INTO venda (fk_cliente, status) VALUES
(1, 'EM_ABERTO');   -- venda 1: Maria Silva, parcelada, ainda em aberto

INSERT INTO venda (fk_cliente, status) VALUES
(2, 'PAGO');        -- venda 2: João Pedro, paga à vista

-- ITEM_VENDA (venda 1 itemizada produto a produto; venda 2 lançada só com o valor total)
INSERT INTO item_venda (fk_venda, nome, quantidade, valor_unitario, valor_total) VALUES
(1, 'Arroz 5kg', 2, 32.50, 65.00);

INSERT INTO item_venda (fk_venda, nome, quantidade, valor_unitario, valor_total) VALUES
(2, 'Produtos', 1, 67.00, 67.00);

-- PAGAMENTO (1:1 com venda)
INSERT INTO pagamento (fk_venda, forma_pagamento, quantidade_parcelas, juros_mes, valor_entrada) VALUES
(1, 'CREDITO', 2, 5.00, 0.00);

INSERT INTO pagamento (fk_venda, forma_pagamento, quantidade_parcelas, juros_mes, valor_entrada) VALUES
(2, 'A_VISTA', 1, 0.00, 67.00);

-- PARCELA (só o pagamento parcelado, id 1, tem parcelas — o à vista já foi quitado)
INSERT INTO parcela (fk_pagamento, numero, valor, data_pagamento, status) VALUES
(1, 1, 33.00, NULL, 'EM_ABERTO');

INSERT INTO parcela (fk_pagamento, numero, valor, data_pagamento, status) VALUES
(1, 2, 33.00, NULL, 'EM_ABERTO');