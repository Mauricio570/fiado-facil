DROP TABLE IF EXISTS parcela CASCADE;
DROP TABLE IF EXISTS pagamento CASCADE;
DROP TABLE IF EXISTS item_venda CASCADE;
DROP TABLE IF EXISTS venda CASCADE;
DROP TABLE IF EXISTS cliente CASCADE;
DROP TABLE IF EXISTS preferencia CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;

CREATE TABLE usuario (
    id              BIGSERIAL PRIMARY KEY,
    nome_empresa    VARCHAR(150) NOT NULL,
    cnpj            VARCHAR(18)  NOT NULL UNIQUE,
    email           VARCHAR(150) NOT NULL UNIQUE,
    senha           VARCHAR(255) NOT NULL,
    data_criacao    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE preferencia (
    id              BIGSERIAL PRIMARY KEY,
    fk_usuario      BIGINT      NOT NULL UNIQUE REFERENCES usuario(id) ON DELETE CASCADE,
    tamanho_texto   VARCHAR(20) NOT NULL DEFAULT 'PADRAO',
    data_criacao    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cliente (
    id              BIGSERIAL PRIMARY KEY,
    fk_usuario      BIGINT       NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    nome            VARCHAR(150) NOT NULL,
    telefone        VARCHAR(20),
    cpf             VARCHAR(14),
    endereco        VARCHAR(255),
    data_criacao    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
    data_vencimento DATE          NOT NULL,
    data_pagamento  DATE,
    status          VARCHAR(20)   NOT NULL DEFAULT 'EM_ABERTO'
);

CREATE INDEX idx_cliente_fk_usuario ON cliente(fk_usuario);
CREATE INDEX idx_venda_fk_cliente ON venda(fk_cliente);
CREATE INDEX idx_item_venda_fk_venda ON item_venda(fk_venda);
CREATE INDEX idx_parcela_fk_pagamento ON parcela(fk_pagamento);
CREATE INDEX idx_parcela_data_vencimento ON parcela(data_vencimento);

INSERT INTO usuario (nome_empresa, cnpj, email, senha) VALUES
('Mercearia Bom Preço', '12.345.678/0001-90', 'teste@gmail.com',
 '$2b$12$8e/fhDONKWTwNtVg5NtMYezl9oZp.tWdoPtna5Vn4K2eC4.bWwM1a');

INSERT INTO usuario (nome_empresa, cnpj, email, senha) VALUES
('Padaria Pão Dourado', '98.765.432/0001-10', 'teste2@gmail.com',
 '$2b$12$2CjpByidNAmQCsBpaOFToOJhujwbu01uQyV/3/Win331Yt/cQfcGu');

INSERT INTO preferencia (fk_usuario, tamanho_texto) VALUES (1, 'PADRAO');
INSERT INTO preferencia (fk_usuario, tamanho_texto) VALUES (2, 'PADRAO');

INSERT INTO cliente (fk_usuario, nome, telefone, cpf, endereco) VALUES
(1, 'Maria Silva', '(51) 99999-1111', '123.456.789-00', 'Rua das Flores, 123 - Porto Alegre/RS');

INSERT INTO cliente (fk_usuario, nome, telefone, cpf, endereco) VALUES
(1, 'João Pedro', '(51) 98888-2222', '987.654.321-00', 'Av. Brasil, 456 - Porto Alegre/RS');

INSERT INTO venda (fk_cliente, status) VALUES
(1, 'EM_ABERTO');

INSERT INTO venda (fk_cliente, status) VALUES
(2, 'PAGO');

INSERT INTO item_venda (fk_venda, nome, quantidade, valor_unitario, valor_total) VALUES
(1, 'Arroz 5kg', 2, 32.50, 65.00);

INSERT INTO item_venda (fk_venda, nome, quantidade, valor_unitario, valor_total) VALUES
(2, 'Produtos', 1, 67.00, 67.00);

INSERT INTO pagamento (fk_venda, forma_pagamento, quantidade_parcelas, juros_mes, valor_entrada) VALUES
(1, 'CREDITO', 2, 5.00, 0.00);

INSERT INTO pagamento (fk_venda, forma_pagamento, quantidade_parcelas, juros_mes, valor_entrada) VALUES
(2, 'A_VISTA', 1, 0.00, 67.00);

INSERT INTO parcela (fk_pagamento, numero, valor, data_vencimento, data_pagamento, status) VALUES
(1, 1, 33.00, CURRENT_DATE + INTERVAL '1 month', NULL, 'EM_ABERTO');

INSERT INTO parcela (fk_pagamento, numero, valor, data_vencimento, data_pagamento, status) VALUES
(1, 2, 33.00, CURRENT_DATE + INTERVAL '2 month', NULL, 'EM_ABERTO');